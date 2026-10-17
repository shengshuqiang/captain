package com.shuqiang.captain.voice;

import android.app.Activity;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

import captain.R;

/** Same-window capture surface: showing it must not steal the hold gesture's pointer. */
final class VoiceCaptureOverlay {
    private static final long CLOCK_TICK_MS = 1000;
    private final View root, progress, stop;
    private final TextView heading, tip;
    private final EditText candidate;
    private final Button confirm;
    private final VoiceNotebookSession session;
    private final ViewGroup parent;
    private final Map<View, Integer> underlyingAccessibility = new LinkedHashMap<>();
    private boolean changing, cancelSelected, runningClock;
    private long startedAt;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!runningClock) return;
            renderHeading();
            root.postDelayed(this, CLOCK_TICK_MS);
        }
    };

    VoiceCaptureOverlay(Activity activity, VoiceNotebookSession session) {
        this.session = session;
        parent = activity.findViewById(android.R.id.content);
        root = activity.getLayoutInflater().inflate(R.layout.voice_capture_overlay, parent, false);
        parent.addView(root);
        root.setVisibility(View.GONE);
        heading = root.findViewById(R.id.voice_capture_heading);
        tip = root.findViewById(R.id.voice_capture_tip);
        candidate = root.findViewById(R.id.voice_capture_editor);
        progress = root.findViewById(R.id.voice_capture_progress);
        stop = root.findViewById(R.id.voice_capture_stop);
        confirm = root.findViewById(R.id.voice_capture_confirm);
        candidate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!changing) session.updatePendingText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        root.findViewById(R.id.voice_capture_cancel).setOnClickListener(v -> session.cancelCapture());
        stop.setOnClickListener(v -> session.stopRecording());
        confirm.setOnClickListener(v -> session.confirmCapture());
    }

    void selectCancel(boolean selected) {
        cancelSelected = selected;
        renderHeading();
    }

    void bind() {
        boolean visible = session.hasPendingCapture();
        if (!visible) { suspend(); return; }
        if (root.getVisibility() != View.VISIBLE) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child == root) continue;
                underlyingAccessibility.put(child, child.getImportantForAccessibility());
                child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            }
        }
        root.setVisibility(View.VISIBLE);
        changing = true;
        if (!candidate.getText().toString().equals(session.pendingText())) {
            candidate.setText(session.pendingText());
            candidate.setSelection(candidate.length());
        }
        changing = false;
        boolean review = session.reviewReady();
        candidate.setFocusable(review);
        candidate.setFocusableInTouchMode(review);
        candidate.setCursorVisible(review);
        candidate.setLongClickable(review);
        candidate.setHint(review ? R.string.voice_capture_empty_hint : R.string.voice_capture_listening_hint);
        confirm.setVisibility(review ? View.VISIBLE : View.GONE);
        confirm.setEnabled(session.canConfirmCapture());
        stop.setVisibility(session.recording() ? View.VISIBLE : View.GONE);
        progress.setVisibility(review ? View.GONE : View.VISIBLE);
        if (review) tip.setText(session.status());
        else tip.setText(R.string.voice_capture_slide_hint);
        if (session.recording() && !runningClock) {
            startedAt = SystemClock.elapsedRealtime();
            runningClock = true;
            root.post(tick);
        } else if (!session.recording()) {
            runningClock = false;
            root.removeCallbacks(tick);
        }
        renderHeading();
    }

    private void renderHeading() {
        if (session.recording()) {
            long seconds = (SystemClock.elapsedRealtime() - startedAt) / CLOCK_TICK_MS;
            String label = root.getContext().getString(cancelSelected
                    ? R.string.voice_capture_discard : R.string.voice_capture_recording);
            heading.setText(String.format(Locale.getDefault(), "%s  %02d:%02d", label, seconds / 60, seconds % 60));
        } else heading.setText(session.reviewReady()
                ? R.string.voice_capture_review : R.string.voice_capture_stopping);
        heading.setTextColor(root.getResources().getColor(cancelSelected
                ? R.color.captain_brand_primary : R.color.captain_surface_card));
    }

    void suspend() {
        if (root.getVisibility() == View.VISIBLE) {
            InputMethodManager keyboard = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(candidate.getWindowToken(), 0);
            candidate.clearFocus();
        }
        for (Map.Entry<View, Integer> entry : underlyingAccessibility.entrySet()) {
            entry.getKey().setImportantForAccessibility(entry.getValue());
        }
        underlyingAccessibility.clear();
        runningClock = false;
        cancelSelected = false;
        root.removeCallbacks(tick);
        root.setVisibility(View.GONE);
    }
}
