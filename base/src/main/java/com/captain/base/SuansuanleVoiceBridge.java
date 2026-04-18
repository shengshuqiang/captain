package com.captain.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 为算算乐 H5 提供语音播报与单次语音指令识别能力。
 */
public class SuansuanleVoiceBridge implements TextToSpeech.OnInitListener, RecognitionListener {
    public interface PermissionDelegate {
        boolean hasRecordAudioPermission();
        void requestRecordAudioPermission();
    }

    private final Activity activity;
    private final WebView webView;
    private final PermissionDelegate permissionDelegate;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextToSpeech textToSpeech;
    private boolean ttsInitRequested;
    private boolean ttsReady;
    private String pendingSpeakText;
    private boolean pendingSpeakInterrupt = true;

    private SpeechRecognizer speechRecognizer;
    private Intent recognitionIntent;
    private boolean listening;
    private boolean suppressClientError;
    private boolean shouldStartAfterPermission;

    public SuansuanleVoiceBridge(
            Activity activity,
            WebView webView,
            PermissionDelegate permissionDelegate
    ) {
        this.activity = activity;
        this.webView = webView;
        this.permissionDelegate = permissionDelegate;
    }

    @JavascriptInterface
    public void speak(String text, boolean interrupt) {
        mainHandler.post(() -> speakInternal(text, interrupt));
    }

    @JavascriptInterface
    public void stopSpeak() {
        mainHandler.post(this::stopSpeakInternal);
    }

    @JavascriptInterface
    public void startVoiceCommand() {
        mainHandler.post(this::startVoiceCommandInternal);
    }

    @JavascriptInterface
    public void stopVoiceCommand() {
        mainHandler.post(this::stopVoiceCommandInternal);
    }

    @JavascriptInterface
    public boolean isVoiceAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(activity.getApplicationContext());
    }

    public void onPermissionResult(boolean granted) {
        mainHandler.post(() -> {
            if (granted) {
                emitStatus("permission_granted", "麦克风权限已开启");
                if (shouldStartAfterPermission) {
                    shouldStartAfterPermission = false;
                    startVoiceCommandInternal();
                }
            } else {
                shouldStartAfterPermission = false;
                emitError("permission_denied", "未开启麦克风权限，语音控制不可用");
            }
        });
    }

    public void destroy() {
        mainHandler.post(() -> {
            stopVoiceCommandInternal();
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
                speechRecognizer = null;
            }
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
                textToSpeech = null;
            }
            ttsReady = false;
        });
    }

    @Override
    public void onInit(int status) {
        ttsReady = status == TextToSpeech.SUCCESS && textToSpeech != null;
        if (!ttsReady) {
            emitError("tts_unavailable", "当前设备不可用语音播报");
            return;
        }
        try {
            textToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE);
        } catch (Exception ignored) {
            // 系统自行回退默认语言。
        }
        if (!TextUtils.isEmpty(pendingSpeakText)) {
            String nextText = pendingSpeakText;
            boolean nextInterrupt = pendingSpeakInterrupt;
            pendingSpeakText = null;
            speakInternal(nextText, nextInterrupt);
        }
    }

    private void initTextToSpeechIfNeeded() {
        if (ttsInitRequested) {
            return;
        }
        ttsInitRequested = true;
        textToSpeech = new TextToSpeech(activity.getApplicationContext(), this);
    }

    private void ensureSpeechRecognizer() {
        if (speechRecognizer != null || !SpeechRecognizer.isRecognitionAvailable(activity.getApplicationContext())) {
            return;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity.getApplicationContext());
        speechRecognizer.setRecognitionListener(this);
        recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognitionIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
    }

    private void speakInternal(String text, boolean interrupt) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        initTextToSpeechIfNeeded();
        if (!ttsReady || textToSpeech == null) {
            pendingSpeakText = text;
            pendingSpeakInterrupt = interrupt;
            return;
        }
        int queueMode = interrupt ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        if (interrupt) {
            textToSpeech.stop();
        }
        textToSpeech.speak(
                text,
                queueMode,
                null,
                "suansuanle-" + SystemClock.uptimeMillis()
        );
    }

    private void stopSpeakInternal() {
        pendingSpeakText = null;
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    private void startVoiceCommandInternal() {
        stopSpeakInternal();
        if (!SpeechRecognizer.isRecognitionAvailable(activity.getApplicationContext())) {
            emitError("recognition_unavailable", "当前设备不支持语音识别");
            return;
        }
        if (!permissionDelegate.hasRecordAudioPermission()) {
            shouldStartAfterPermission = true;
            emitStatus("awaiting_permission", "请允许麦克风权限");
            permissionDelegate.requestRecordAudioPermission();
            return;
        }
        ensureSpeechRecognizer();
        if (speechRecognizer == null || recognitionIntent == null) {
            emitError("recognition_unavailable", "当前设备不支持语音识别");
            return;
        }
        try {
            if (listening) {
                return;
            }
            listening = true;
            suppressClientError = false;
            emitStatus("listening", "正在聆听，请说数字、退格或提交");
            speechRecognizer.startListening(recognitionIntent);
        } catch (SecurityException e) {
            listening = false;
            emitError("permission_denied", "未开启麦克风权限，语音控制不可用");
        } catch (Exception e) {
            listening = false;
            emitError("start_failed", "语音识别启动失败");
        }
    }

    private void stopVoiceCommandInternal() {
        shouldStartAfterPermission = false;
        if (speechRecognizer != null) {
            try {
                suppressClientError = listening;
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
            } catch (Exception ignored) {
                // 忽略状态切换期间的底层异常。
            }
        }
        if (listening) {
            emitStatus("stopped", "已停止语音输入");
        }
        listening = false;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        emitStatus("ready", "请开始说话");
    }

    @Override
    public void onBeginningOfSpeech() {
        emitStatus("hearing", "已开始收音");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // H5 不关心音量变化。
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // 无需处理底层音频缓冲。
    }

    @Override
    public void onEndOfSpeech() {
        emitStatus("processing", "正在识别");
    }

    @Override
    public void onError(int error) {
        listening = false;
        if (error == SpeechRecognizer.ERROR_CLIENT && suppressClientError) {
            suppressClientError = false;
            emitStatus("stopped", "已停止语音输入");
            return;
        }
        suppressClientError = false;
        emitError(mapErrorCode(error), mapErrorMessage(error));
    }

    @Override
    public void onResults(Bundle results) {
        listening = false;
        ArrayList<String> matches = results == null
                ? null
                : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String transcript = matches == null || matches.isEmpty() ? "" : matches.get(0);
        JSONObject payload = new JSONObject();
        try {
            payload.put("transcript", transcript);
            JSONArray candidates = new JSONArray();
            if (matches != null) {
                for (String match : matches) {
                    candidates.put(match);
                }
            }
            payload.put("candidates", candidates);
        } catch (JSONException ignored) {
            // 透传失败时至少保证首条识别文本可用。
        }
        emitEvent("result", payload);
        emitStatus("stopped", "识别完成");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // 单次命令模式不处理中间结果。
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // 无额外事件处理。
    }

    private String mapErrorCode(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "audio_error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "client_error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "permission_denied";
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "network_error";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "no_match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "recognizer_busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "server_error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "speech_timeout";
            default:
                return "unknown_error";
        }
    }

    private String mapErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "录音失败，请重试";
            case SpeechRecognizer.ERROR_CLIENT:
                return "语音识别已取消";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "未开启麦克风权限，语音控制不可用";
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "网络异常，语音识别失败";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "没听清，请再说一次";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "语音识别忙，请稍后再试";
            case SpeechRecognizer.ERROR_SERVER:
                return "语音服务暂时不可用";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "长时间未说话，请重试";
            default:
                return "语音识别失败，请重试";
        }
    }

    private void emitStatus(String state, String message) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("state", state);
            payload.put("message", message);
        } catch (JSONException ignored) {
            // 仅透传基础状态，不影响功能。
        }
        emitEvent("status", payload);
    }

    private void emitError(String code, String message) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("code", code);
            payload.put("message", message);
        } catch (JSONException ignored) {
            // 错误透传失败时不阻塞主流程。
        }
        emitEvent("error", payload);
    }

    private void emitEvent(String type, JSONObject payload) {
        JSONObject event = new JSONObject();
        try {
            event.put("type", type);
            event.put("payload", payload == null ? new JSONObject() : payload);
        } catch (JSONException ignored) {
            // 外层事件包装失败时直接放弃本次通知。
            return;
        }
        webView.post(() -> webView.evaluateJavascript(
                "(function(){var api=window.SuanSuanLeNative;"
                        + "if(api&&typeof api.onVoiceEvent==='function'){"
                        + "api.onVoiceEvent(" + event.toString() + ");"
                        + "}})();",
                null
        ));
    }
}
