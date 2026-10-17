package com.shuqiang.captain.voice;

import java.util.ArrayList;
import java.util.List;

/** Pure input contracts: long hold, slide cancellation, interruptions and denied start. */
public final class VoiceHoldGestureChecks {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    private static final class Actions implements VoiceHoldGesture.Listener {
        int starts;
        boolean allowed = true;
        final List<Boolean> releases = new ArrayList<>(), selections = new ArrayList<>();
        @Override public boolean onStart() { starts++; return allowed; }
        @Override public void onCancelSelection(boolean selected) { selections.add(selected); }
        @Override public void onRelease(boolean discard) { releases.add(discard); }
    }

    public static void main(String[] args) {
        Actions actions = new Actions();
        VoiceHoldGesture gesture = new VoiceHoldGesture(actions, 72, 8);
        gesture.press(500);
        equal(false, gesture.release());
        equal(0, actions.starts);
        equal(0, actions.releases.size());
        equal(false, gesture.activate());

        gesture.press(500);
        equal(true, gesture.activate());
        equal(false, gesture.activate());
        equal(1, actions.starts);
        equal(true, gesture.release());
        equal(false, actions.releases.get(0));
        gesture.release();
        equal(1, actions.releases.size());

        gesture.press(500);
        gesture.activate();
        gesture.move(427);
        equal(true, actions.selections.get(actions.selections.size() - 1));
        gesture.release();
        equal(true, actions.releases.get(1));

        gesture.press(500);
        gesture.activate();
        gesture.move(428);
        int selectionCount = actions.selections.size();
        gesture.move(433);
        equal(selectionCount, actions.selections.size());
        gesture.move(437);
        equal(false, actions.selections.get(actions.selections.size() - 1));
        gesture.release();
        equal(false, actions.releases.get(2));

        // Moving into cancellation before the long-press timeout must never open the microphone.
        gesture.press(500);
        gesture.move(420);
        int starts = actions.starts;
        equal(false, gesture.activate());
        equal(starts, actions.starts);
        equal(true, gesture.release());
        equal(3, actions.releases.size());

        // Navigation and system interruptions stop the microphone but preserve a review candidate.
        gesture.press(500);
        gesture.activate();
        gesture.move(400);
        gesture.interrupt();
        equal(false, actions.releases.get(3));
        gesture.interrupt();
        equal(4, actions.releases.size());

        actions.allowed = false;
        gesture.press(500);
        equal(false, gesture.activate());
        equal(true, gesture.release());
        equal(4, actions.releases.size());
        equal(false, gesture.activate());
        System.out.println("Passed " + checks + " voice hold gesture checks");
    }
}
