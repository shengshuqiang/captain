package com.shuqiang.captain.voice;

/** Partial hypotheses replace one segment; final results commit that segment once. */
public final class TranscriptBuffer {
    private String committed, partial = "";
    public TranscriptBuffer(String initial) { committed = initial; }

    public String update(String text, boolean finalSegment) {
        if (finalSegment && !text.isEmpty()) {
            committed = append(committed, text);
            partial = "";
        } else if (!finalSegment && !text.isEmpty()) partial = text;
        return append(committed, partial);
    }

    private static String append(String before, String text) {
        if (text.isEmpty()) return before;
        return before.isEmpty() ? text : before + "\n" + text;
    }
}
