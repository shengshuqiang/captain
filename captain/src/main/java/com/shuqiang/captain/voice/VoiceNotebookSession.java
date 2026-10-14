package com.shuqiang.captain.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/** Application-owned draft: recording can finish saving after its screen is destroyed. */
public final class VoiceNotebookSession implements OfflineVoiceEngine.Listener {
    interface Observer { void onChanged(boolean refreshNotes); }
    private static final String DRAFT_KEY = "draft";
    private static final String SAVE_FAILED = "保存失败，请检查本机可用空间；文字仍保留在草稿中。";
    private static VoiceNotebookSession instance;
    private final Context context;
    private final VoiceNoteStore store;
    private final SharedPreferences preferences;
    private OfflineVoiceEngine engine;
    private Uri pendingModel;
    private Observer observer;
    private VoiceNote current;
    private TranscriptBuffer transcript;
    private String text = "", status = "首次使用请导入中文模型，之后可断网识别。";
    private boolean draftLoaded, ready, recording, stopping, loading, closing, recognitionError;

    public static VoiceNotebookSession get(Context context) {
        if (instance == null) instance = new VoiceNotebookSession(context.getApplicationContext());
        return instance;
    }

    private VoiceNotebookSession(Context context) {
        this.context = context;
        store = new VoiceNoteStore(context);
        preferences = context.getSharedPreferences("voice-notebook", Context.MODE_PRIVATE);
    }

    void attach(Observer observer) {
        this.observer = observer;
        if (!draftLoaded) restoreDraft();
        if (!closing && engine == null) openEngine();
        changed(true);
    }

    void detach(Observer observer) {
        if (this.observer != observer) return;
        this.observer = null;
        stopRecording();
        if (engine != null && !closing) {
            closing = true;
            engine.close();
        }
    }

    private void openEngine() {
        engine = new OfflineVoiceEngine(context, this);
        if (pendingModel != null) beginImport();
        else if (engine.installed()) {
            loading = true;
            status = "正在加载本机模型…";
            engine.load();
        }
    }

    private boolean restoreDraft() {
        try {
            String id = preferences.getString(DRAFT_KEY, null);
            current = id == null ? null : store.find(id);
            text = current == null ? "" : current.text;
            draftLoaded = true;
            return true;
        } catch (RuntimeException e) {
            fail("draft_read_failed", "读取草稿失败，请释放空间后点击重试读取。");
            return false;
        }
    }

    boolean persist() {
        if (!draftLoaded) return false;
        try {
            if (text.trim().isEmpty()) {
                if (current != null) store.delete(current.id);
                if (!preferences.edit().remove(DRAFT_KEY).commit()) throw new IllegalStateException();
                current = null;
            } else {
                if (current == null) {
                    long now = System.currentTimeMillis();
                    current = new VoiceNote(UUID.randomUUID().toString(),
                            VoiceNote.dayAt(now, TimeZone.getDefault()), now, text);
                } else current = new VoiceNote(current.id, current.day, current.createdAt, text);
                store.save(current);
                if (!preferences.edit().putString(DRAFT_KEY, current.id).commit()) throw new IllegalStateException();
            }
            return true;
        } catch (RuntimeException e) {
            fail("draft_save_failed", SAVE_FAILED);
            return false;
        }
    }

    void updateText(String value) {
        if (!draftLoaded || busy()) return;
        text = value;
        persist();
    }

    void finishNote() {
        if (busy()) return;
        if (!draftLoaded) {
            if (restoreDraft()) status = "草稿已恢复，可以继续编辑。";
        } else if (persist()) {
            try {
                if (!preferences.edit().remove(DRAFT_KEY).commit()) throw new IllegalStateException();
            } catch (RuntimeException e) {
                fail("draft_complete_failed", SAVE_FAILED);
                return;
            }
            current = null;
            text = "";
            status = "已保存到本机，可以开始新笔记。";
        }
        changed(true);
    }

    void edit(String id) {
        if (busy() || !persist()) return;
        try {
            // Read only after saving: a retry may have just replaced this same note.
            VoiceNote selected = store.find(id);
            if (selected == null) { changed(true); return; }
            if (!preferences.edit().putString(DRAFT_KEY, selected.id).commit()) throw new IllegalStateException();
            current = selected;
            text = selected.text;
            changed(true);
        } catch (RuntimeException e) { fail("note_read_failed", "读取笔记失败，请检查存储空间后重试。"); }
    }

    String shareNote(String id) {
        if (busy() || !persist()) return null;
        try {
            VoiceNote selected = store.find(id);
            return selected == null ? null : VoiceNote.shareText(selected.day, Collections.singletonList(selected));
        } catch (RuntimeException e) { fail("note_share_read_failed", "读取分享内容失败，请稍后重试。"); return null; }
    }

    String shareDay(String day) {
        if (busy() || !persist()) return null;
        Map<String, List<VoiceNote>> groups = grouped();
        List<VoiceNote> notes = groups == null ? null : groups.get(day);
        return notes == null ? null : VoiceNote.shareText(day, notes);
    }

    void delete(String id) {
        if (busy()) return;
        try {
            store.delete(id);
            if (current != null && current.id.equals(id)) {
                current = null;
                text = "";
                preferences.edit().remove(DRAFT_KEY).commit();
            }
            changed(true);
        } catch (RuntimeException e) { fail("note_delete_failed", "删除失败，笔记仍保留，请稍后重试。"); }
    }

    Map<String, List<VoiceNote>> grouped() {
        try { return store.grouped(); }
        catch (RuntimeException e) { fail("notes_read_failed", "读取笔记失败，请释放空间后重试。"); return null; }
    }

    void importModel(Uri uri) {
        if (recording || stopping || ready) return;
        // Document results may arrive before onStart or while the previous engine closes.
        pendingModel = uri;
        beginImport();
    }

    private void beginImport() {
        if (pendingModel == null || engine == null || closing || loading) return;
        Uri uri = pendingModel;
        pendingModel = null;
        loading = true;
        status = "正在解压并验证模型，请稍候…";
        engine.importModel(uri);
        changed(false);
    }

    void startRecording() {
        if (busy() || !canRecord() || observer == null || !persist()) return;
        transcript = new TranscriptBuffer(text);
        recognitionError = false;
        recording = true;
        status = "正在离线识别 · 文字自动保存 · 点击停止结束录音";
        engine.start();
        changed(false);
    }

    void stopRecording() {
        if (!recording) return;
        recording = false;
        stopping = true;
        status = "正在保存最后一段文字…";
        engine.stop();
        changed(false);
    }

    @Override public void onReady() {
        loading = false;
        ready = !closing;
        if (!closing) status = "中文模型已就绪 · 识别完全离线 · 文字保存在本机";
        changed(false);
    }

    @Override public void onText(String value, boolean finalSegment) {
        text = transcript.update(value, finalSegment);
        persist();
        changed(false);
    }

    @Override public void onStopped() {
        recording = false;
        stopping = false;
        if (persist() && !recognitionError) status = "录音已结束 · 文字已保存到本机";
        changed(true);
    }

    @Override public void onError(String message) {
        loading = false;
        recognitionError = true;
        fail("recognition_failed", message);
    }

    @Override public void onClosed() {
        // FIFO main-thread callbacks guarantee final text has been saved first.
        engine = null;
        closing = ready = loading = stopping = recording = false;
        store.close();
        if (observer != null) openEngine();
        changed(true);
    }

    String text() { return text; }
    String status() { return status; }
    boolean busy() { return recording || stopping || closing; }
    boolean recording() { return recording; }
    boolean draftLoaded() { return draftLoaded; }
    boolean canRecord() { return draftLoaded && ready && !stopping && !closing; }
    boolean canImport() { return engine != null && !loading && !closing && !ready && !busy(); }
    private void changed(boolean refreshNotes) { if (observer != null) observer.onChanged(refreshNotes); }
    private void fail(String reason, String message) {
        Log.w("VoiceNotebook", "reason=" + reason);
        status = message;
        changed(false);
    }
}
