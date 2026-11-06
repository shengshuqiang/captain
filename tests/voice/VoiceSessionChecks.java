package com.shuqiang.captain.voice;

import android.content.Context;
import android.net.Uri;
import java.lang.reflect.Constructor;

/** Fault recovery and late-result contracts; platform IO is replaced by explicit doubles. */
public final class VoiceSessionChecks {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    private static void absent(Object value) { equal(true, value == null); }
    private static VoiceNotebookSession session(Context context) throws Exception {
        Constructor<VoiceNotebookSession> constructor = VoiceNotebookSession.class.getDeclaredConstructor(Context.class);
        constructor.setAccessible(true);
        return constructor.newInstance(context);
    }

    public static void main(String[] args) throws Exception {
        Context context = new Context();
        VoiceNotebookSession session = session(context);
        int[] oldUpdates = {0};
        VoiceNotebookSession.Observer first = refresh -> oldUpdates[0]++;
        session.attach(first);
        OfflineVoiceEngine engine = OfflineVoiceEngine.last;
        VoiceNoteStore store = VoiceNoteStore.last;
        engine.listener.onReady();
        session.updateText("已有文字");
        String id = context.prefs.getString("draft", "");
        session.startRecording();
        session.startRecording();
        equal(1, engine.starts);
        engine.listener.onText("最后", false);
        session.detach(first);
        int updatesAtDetach = oldUpdates[0];
        equal(true, engine.closing);
        equal(0, store.closeCount);
        session.attach(refresh -> { });
        equal(false, session.canRecord());
        // The old Activity is gone; final text must still replace its partial and reach storage.
        engine.listener.onText("最后一句", true);
        engine.listener.onStopped();
        equal("已有文字\n最后一句", session.text());
        equal(session.text(), store.data.get(id).text);
        equal(1, store.data.size());
        equal(0, store.closeCount);
        equal(updatesAtDetach, oldUpdates[0]);
        engine.listener.onClosed();
        equal(1, store.closeCount);
        equal(false, engine == OfflineVoiceEngine.last);
        OfflineVoiceEngine.last.listener.onReady();
        equal(true, session.canRecord());

        // Saving failure must neither share an older database value nor erase the draft.
        store.failWrites = true;
        session.updateText("尚未保存的新文字");
        absent(session.shareNote(id));
        absent(session.shareDay(store.data.get(id).day));
        equal("尚未保存的新文字", session.text());
        equal("已有文字\n最后一句", store.data.get(id).text);
        store.failWrites = false;
        session.edit(id);
        equal("尚未保存的新文字", session.text());
        equal("尚未保存的新文字", store.data.get(id).text);
        equal(true, session.shareNote(id).endsWith("尚未保存的新文字"));
        equal(true, session.shareDay(store.data.get(id).day).endsWith("尚未保存的新文字"));

        // Deletion and list-read failures leave the stored note and editor available for retry.
        store.failDeletes = true;
        session.delete(id);
        equal(1, store.data.size());
        equal("尚未保存的新文字", session.text());
        store.failReads = true;
        absent(session.grouped());
        session.edit(id);
        equal("尚未保存的新文字", session.text());
        store.failReads = false;
        equal(1, session.grouped().size());
        store.failDeletes = false;
        session.delete(id);
        equal(0, store.data.size());
        equal("", session.text());

        // A failed draft read disables editing until the same data can be restored successfully.
        Context recovery = new Context();
        recovery.failReads = true;
        recovery.prefs.data.put("draft", "existing");
        VoiceNotebookSession recovered = session(recovery);
        VoiceNoteStore recoveryStore = VoiceNoteStore.last;
        recoveryStore.data.put("existing", new VoiceNote("existing", "2026-10-15", 1L, "磁盘上的原文"));
        recovered.attach(refresh -> { });
        equal(false, recovered.draftLoaded());
        recovered.updateText("不得覆盖原文");
        equal("磁盘上的原文", recoveryStore.data.get("existing").text);
        recoveryStore.failReads = false;
        recovered.finishNote();
        equal(true, recovered.draftLoaded());
        equal("磁盘上的原文", recovered.text());
        equal(1, recoveryStore.data.size());

        // File-picker results are delivered before onStart, including an engine still draining.
        VoiceNotebookSession imported = session(new Context());
        Uri firstModel = Uri.parse("content://model/first");
        imported.importModel(firstModel);
        VoiceNotebookSession.Observer screen = refresh -> { };
        imported.attach(screen);
        OfflineVoiceEngine importEngine = OfflineVoiceEngine.last;
        equal(firstModel, importEngine.imported);
        imported.detach(screen);
        Uri nextModel = Uri.parse("content://model/next");
        imported.importModel(nextModel);
        importEngine.listener.onClosed();
        imported.attach(screen);
        equal(nextModel, OfflineVoiceEngine.last.imported);
        System.out.println("Passed " + checks + " voice session fault/lifecycle checks (platform IO doubles)");
    }
}
