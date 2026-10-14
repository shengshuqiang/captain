package com.shuqiang.captain.voice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** A note keeps its creation-day bucket even after editing or changing timezone. */
public final class VoiceNote {
    public final String id, day, text;
    public final long createdAt;

    public VoiceNote(String id, String day, long createdAt, String text) {
        this.id = id;
        this.day = day;
        this.createdAt = createdAt;
        this.text = text;
    }

    public static String dayAt(long timestamp, TimeZone zone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(zone);
        return format.format(new Date(timestamp));
    }

    public static String shareText(String day, Iterable<VoiceNote> notes) {
        StringBuilder out = new StringBuilder("语音记事本 · ").append(day);
        for (VoiceNote note : notes) {
            out.append("\n\n").append(note.text);
        }
        return out.toString();
    }
}
