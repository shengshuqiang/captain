#!/usr/bin/env python3
"""Run the real session against deterministic platform/IO doubles, without an APK."""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
FIXTURES = {
    "android/content/Context.java": """package android.content;
public class Context {
    public static final int MODE_PRIVATE = 0;
    public final SharedPreferences prefs = new SharedPreferences();
    public boolean failReads;
    public Context getApplicationContext() { return this; }
    public SharedPreferences getSharedPreferences(String name, int mode) { return prefs; }
}
""",
    "android/content/SharedPreferences.java": """package android.content;
import java.util.*;
public class SharedPreferences {
    public final Map<String,String> data = new HashMap<>();
    public boolean fail;
    public String getString(String key, String fallback) { return data.getOrDefault(key, fallback); }
    public Editor edit() { return new Editor(); }
    public class Editor {
        final Map<String,String> next = new HashMap<>(data);
        public Editor putString(String key, String value) { next.put(key,value); return this; }
        public Editor remove(String key) { next.remove(key); return this; }
        public boolean commit() { if (fail) return false; data.clear(); data.putAll(next); return true; }
    }
}
""",
    "android/net/Uri.java": """package android.net;
public final class Uri { public static Uri parse(String value) { return new Uri(); } }
""",
    "android/util/Log.java": """package android.util;
public final class Log { public static int w(String tag, String message) { return 0; } }
""",
    "com/shuqiang/captain/voice/VoiceNoteStore.java": """package com.shuqiang.captain.voice;
import android.content.Context;
import java.util.*;
public final class VoiceNoteStore {
    public static VoiceNoteStore last;
    public final Map<String,VoiceNote> data = new LinkedHashMap<>();
    public boolean failWrites, failReads, failDeletes;
    public int closeCount;
    public VoiceNoteStore(Context context) { last=this; failReads=context.failReads; }
    public void save(VoiceNote note) { if(failWrites) throw new RuntimeException(); data.put(note.id,note); }
    public VoiceNote find(String id) { if(failReads) throw new RuntimeException(); return data.get(id); }
    public void delete(String id) { if(failDeletes) throw new RuntimeException(); data.remove(id); }
    public Map<String,List<VoiceNote>> grouped() {
        if(failReads) throw new RuntimeException();
        Map<String,List<VoiceNote>> groups = new LinkedHashMap<>();
        for(VoiceNote note:data.values()) groups.computeIfAbsent(note.day,k->new ArrayList<>()).add(note);
        return groups;
    }
    public void close() { closeCount++; }
}
""",
    "com/shuqiang/captain/voice/OfflineVoiceEngine.java": """package com.shuqiang.captain.voice;
import android.content.Context;
import android.net.Uri;
public final class OfflineVoiceEngine {
    public interface Listener {
        void onReady(); void onText(String text,boolean finalSegment);
        void onStopped(); void onError(String message); void onClosed();
    }
    public static OfflineVoiceEngine last;
    public final Listener listener;
    public int starts;
    public boolean closing;
    public Uri imported;
    public OfflineVoiceEngine(Context context,Listener listener) { last=this; this.listener=listener; }
    public boolean installed() { return false; }
    public void load() { }
    public void importModel(Uri uri) { imported=uri; }
    public void start() { starts++; }
    public void stop() { }
    public void close() { closing=true; }
}
""",
}

with tempfile.TemporaryDirectory(prefix="voice-session-checks-") as directory:
    temporary = Path(directory)
    files = []
    for name, content in FIXTURES.items():
        path = temporary / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        files.append(str(path))
    voice = ROOT / "captain/src/main/java/com/shuqiang/captain/voice"
    files += [str(voice / name) for name in (
        "VoiceNote.java", "TranscriptBuffer.java", "VoiceNotebookSession.java")]
    files.append(str(ROOT / "tests/voice/VoiceSessionChecks.java"))
    classes = temporary / "classes"
    classes.mkdir()
    subprocess.run(["javac", "-encoding", "UTF-8", "--release", "8", "-d", str(classes), *files], check=True)
    subprocess.run(["java", "-cp", str(classes), "com.shuqiang.captain.voice.VoiceSessionChecks"], check=True)
