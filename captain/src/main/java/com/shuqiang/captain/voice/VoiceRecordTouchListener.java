package com.shuqiang.captain.voice;

import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import captain.R;

/** Keeps the initial touch target alive while the recording overlay appears in the same window. */
final class VoiceRecordTouchListener implements View.OnTouchListener {
    private static final int NO_POINTER = -1;
    private final View target;
    private final VoiceHoldGesture gesture;
    private int pointerId = NO_POINTER;
    private final int[] location = new int[2];
    private final Runnable activate;

    VoiceRecordTouchListener(View target, VoiceHoldGesture.Listener listener) {
        this.target = target;
        gesture = new VoiceHoldGesture(listener,
                target.getResources().getDimension(R.dimen.voice_cancel_distance),
                target.getResources().getDimension(R.dimen.voice_cancel_hysteresis));
        activate = () -> {
            if (gesture.activate()) target.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        };
    }

    @Override public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pointerId = event.getPointerId(0);
                view.setPressed(true);
                gesture.press(event.getRawY());
                view.getParent().requestDisallowInterceptTouchEvent(true);
                view.postDelayed(activate, ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_MOVE:
                int index = event.findPointerIndex(pointerId);
                if (index < 0) { interrupt(); return true; }
                view.getLocationOnScreen(location);
                gesture.move(event.getY(index) + location[1]);
                return true;
            case MotionEvent.ACTION_UP:
                target.removeCallbacks(activate);
                boolean held = gesture.release();
                releaseParent();
                if (!held) view.performClick();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(event.getActionIndex()) == pointerId) interrupt();
                return true;
            case MotionEvent.ACTION_CANCEL:
                interrupt();
                return true;
            default:
                return true;
        }
    }

    void interrupt() {
        target.removeCallbacks(activate);
        gesture.interrupt();
        releaseParent();
    }

    private void releaseParent() {
        pointerId = NO_POINTER;
        target.setPressed(false);
        if (target.getParent() != null) target.getParent().requestDisallowInterceptTouchEvent(false);
    }
}
