package com.shuqiang.captain.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** All model/native/audio operations run on one worker, including resource disposal. */
public final class OfflineVoiceEngine {
    public interface Listener {
        void onReady();
        void onText(String text, boolean finalSegment);
        void onStopped();
        void onError(String message);
        void onClosed();
    }

    private final Context context;
    private final Listener listener;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean recording = new AtomicBoolean();
    private volatile boolean closed;
    private Model model;

    public OfflineVoiceEngine(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    private File modelDir() { return new File(context.getNoBackupFilesDir(), "voice-model"); }
    public boolean installed() { return modelDir().isDirectory(); }

    public void load() {
        if (closed) return;
        worker.execute(() -> {
            try {
                model = new Model(modelDir().getAbsolutePath());
                post(listener::onReady);
            } catch (Exception | LinkageError e) { error(e); }
        });
    }

    public void importModel(Uri uri) {
        if (closed) return;
        worker.execute(() -> {
            File staging = new File(context.getNoBackupFilesDir(), "voice-model-import");
            try {
                ModelArchive.delete(staging);
                File extracted;
                try (InputStream input = context.getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IOException("无法打开模型 ZIP");
                    extracted = ModelArchive.extract(input, staging);
                }
                // Validate with the actual native recognizer before replacing installation.
                try (Model validation = new Model(extracted.getAbsolutePath())) { }
                if (model != null) { model.close(); model = null; }
                ModelArchive.delete(modelDir());
                if (!extracted.renameTo(modelDir())) throw new IOException("无法安装模型，请重试");
                model = new Model(modelDir().getAbsolutePath());
                post(listener::onReady);
            } catch (Exception | LinkageError e) { error(e); }
            finally { ModelArchive.delete(staging); }
        });
    }

    public void start() {
        if (closed) return;
        if (!recording.compareAndSet(false, true)) return;
        worker.execute(() -> {
            AudioRecord audio = null;
            try {
                if (model == null) throw new IOException("请先导入并加载模型");
                int minimum = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (minimum <= 0) throw new IOException("设备不支持 16 kHz 录音");
                audio = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        Math.max(minimum, 6400));
                if (audio.getState() != AudioRecord.STATE_INITIALIZED) throw new IOException("无法初始化麦克风");
                try (Recognizer recognizer = new Recognizer(model, 16000)) {
                    audio.startRecording();
                    if (audio.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                        throw new IOException("麦克风被占用或没有录音权限");
                    }
                    short[] buffer = new short[3200];
                    while (recording.get()) {
                        int read = audio.read(buffer, 0, buffer.length);
                        if (read < 0) throw new IOException("录音失败，请重试");
                        if (read == 0) continue;
                        if (recognizer.acceptWaveForm(buffer, read)) {
                            emit(recognizer.getResult(), "text", true);
                        } else {
                            emit(recognizer.getPartialResult(), "partial", false);
                        }
                    }
                    emit(recognizer.getFinalResult(), "text", true);
                }
            } catch (Exception | LinkageError e) { error(e); }
            finally {
                try {
                    if (audio != null) {
                        try {
                            if (audio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) audio.stop();
                        } finally { audio.release(); }
                    }
                } catch (RuntimeException e) { error(e); }
                finally {
                    recording.set(false);
                    post(listener::onStopped);
                }
            }
        });
    }

    private void emit(String json, String key, boolean finalSegment) throws Exception {
        String text = new JSONObject(json).optString(key, "").trim();
        post(() -> listener.onText(text, finalSegment));
    }

    public void stop() { recording.set(false); }

    public void close() {
        if (closed) return;
        closed = true;
        stop();
        // Drain final text and stop callbacks before the owner closes its database.
        worker.execute(() -> {
            try { if (model != null) { model.close(); model = null; } }
            finally { post(listener::onClosed); }
        });
        worker.shutdown();
    }

    private void post(Runnable action) {
        main.post(action);
    }

    private void error(Throwable error) {
        String message = error.getMessage();
        post(() -> listener.onError(message == null ? "离线识别失败，请检查模型或录音权限" : message));
    }
}
