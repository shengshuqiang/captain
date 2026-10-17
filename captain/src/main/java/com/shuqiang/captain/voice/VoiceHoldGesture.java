package com.shuqiang.captain.voice;

/** One finger owns each hold; interrupted gestures stop safely without publishing a note. */
final class VoiceHoldGesture {
    interface Listener {
        boolean onStart();
        void onCancelSelection(boolean selected);
        void onRelease(boolean discard);
    }

    private final Listener listener;
    private final float cancelDistance, hysteresis;
    private boolean pressed, recording, discard, attempted;
    private float startY, lastY;

    VoiceHoldGesture(Listener listener, float cancelDistance, float hysteresis) {
        this.listener = listener;
        this.cancelDistance = cancelDistance;
        this.hysteresis = hysteresis;
    }

    void press(float y) {
        interrupt();
        pressed = true;
        startY = lastY = y;
    }

    boolean activate() {
        if (!pressed || attempted) return false;
        attempted = true;
        if (startY - lastY >= cancelDistance) return false;
        recording = listener.onStart();
        if (recording) move(lastY);
        return recording;
    }

    void move(float y) {
        lastY = y;
        if (!recording) return;
        float threshold = discard ? cancelDistance - hysteresis : cancelDistance;
        boolean next = startY - y >= threshold;
        if (next != discard) {
            discard = next;
            listener.onCancelSelection(discard);
        }
    }

    boolean release() {
        boolean completedHold = attempted;
        finish(discard);
        return completedHold;
    }

    void interrupt() { finish(false); }

    private void finish(boolean discardResult) {
        boolean wasRecording = recording;
        pressed = recording = discard = attempted = false;
        if (wasRecording) {
            listener.onCancelSelection(false);
            listener.onRelease(discardResult);
        }
    }
}
