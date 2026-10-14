package com.shuqiang.captain.voice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal app storage: no external-storage permission or network required. */
public final class VoiceNoteStore extends SQLiteOpenHelper {
    public VoiceNoteStore(Context context) {
        super(context, "voice-notes.db", null, 1);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (id TEXT PRIMARY KEY, day TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL, text TEXT NOT NULL)");
        db.execSQL("CREATE INDEX notes_day ON notes(day, created_at)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1. Add non-destructive migrations when the schema changes.
    }

    public void save(VoiceNote note) {
        ContentValues values = new ContentValues();
        values.put("id", note.id);
        values.put("day", note.day);
        values.put("created_at", note.createdAt);
        values.put("text", note.text);
        if (getWritableDatabase().insertWithOnConflict("notes", null, values,
                SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            throw new IllegalStateException("无法写入笔记");
        }
    }

    public VoiceNote find(String id) {
        try (Cursor c = getReadableDatabase().query("notes", null, "id=?",
                new String[]{id}, null, null, null)) {
            return c.moveToFirst() ? read(c) : null;
        }
    }

    public void delete(String id) {
        getWritableDatabase().delete("notes", "id=?", new String[]{id});
    }

    public Map<String, List<VoiceNote>> grouped() {
        Map<String, List<VoiceNote>> groups = new LinkedHashMap<>();
        try (Cursor c = getReadableDatabase().query("notes", null, null, null,
                null, null, "day DESC, created_at DESC, id ASC")) {
            while (c.moveToNext()) {
                VoiceNote note = read(c);
                List<VoiceNote> notes = groups.get(note.day);
                if (notes == null) {
                    notes = new ArrayList<>();
                    groups.put(note.day, notes);
                }
                notes.add(note);
            }
        }
        return groups;
    }

    private VoiceNote read(Cursor c) {
        return new VoiceNote(c.getString(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("day")),
                c.getLong(c.getColumnIndexOrThrow("created_at")),
                c.getString(c.getColumnIndexOrThrow("text")));
    }
}
