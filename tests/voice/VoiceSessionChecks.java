package com.shuqiang.captain.voice;

import android.content.Context;
import android.net.Uri;
import java.lang.reflect.Constructor;

/** Exercises the real session's capture contract with deterministic platform and IO doubles. */
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
    private static OfflineVoiceEngine ready(VoiceNotebookSession session) {
        session.attach(refresh -> { });
        OfflineVoiceEngine engine = OfflineVoiceEngine.last;
        engine.listener.onReady();
        return engine;
    }
    private static void stopped(VoiceNotebookSession session, OfflineVoiceEngine engine, String text) {
        session.startRecording();
        engine.listener.onText(text, false);
        session.stopRecording();
        equal(false, session.reviewReady());
        engine.listener.onText(text, true);
        engine.listener.onStopped();
        equal(true, session.reviewReady());
    }

    public static void main(String[] args) throws Exception {
        Context context = new Context();
        VoiceNotebookSession session = session(context);
        OfflineVoiceEngine engine = ready(session);
        VoiceNoteStore store = VoiceNoteStore.last;
        session.updateText("已有文字");
        String id = context.prefs.getString("draft", "");
        VoiceNote original = store.data.get(id);
        session.startRecording();
        int writesAtStart = store.writes;
        session.startRecording();
        equal(1, engine.starts);
        engine.listener.onText("第一", false);
        engine.listener.onText("第一段", true);
        engine.listener.onText("尾", false);
        equal("已有文字", session.text());
        equal("第一段\n尾", session.pendingText());
        equal("已有文字", store.data.get(id).text);
        equal(writesAtStart, store.writes);
        session.stopRecording();
        equal(false, session.reviewReady());
        session.updatePendingText("收尾前不能编辑");
        equal("第一段\n尾", session.pendingText());
        engine.listener.onText("尾句", true);
        engine.listener.onStopped();
        equal(true, session.reviewReady());
        equal("第一段\n尾句", session.pendingText());
        equal(writesAtStart, store.writes);
        session.updateText("不得改正式草稿");
        session.finishNote();
        session.delete(id);
        session.edit("other");
        absent(session.shareNote(id));
        absent(session.shareDay(original.day));
        session.startRecording();
        equal(1, engine.starts);
        equal("已有文字", session.text());
        equal(1, store.data.size());
        equal(false, session.canEditDraft());
        equal(false, session.canRecord());
        equal(false, session.canImport());
        session.updatePendingText("校对后的文字");
        equal(true, session.confirmCapture());
        equal(false, session.confirmCapture());
        equal(false, session.hasPendingCapture());
        equal("已有文字\n校对后的文字", session.text());
        equal(session.text(), store.data.get(id).text);
        equal(original.day, store.data.get(id).day);
        equal(original.createdAt, store.data.get(id).createdAt);
        equal(id, store.data.get(id).id);
        equal(1, store.data.size());
        equal(true, session.canEditDraft());

        // Cancellation while draining ignores late callbacks and keeps the original draft.
        String saved = session.text();
        session.startRecording();
        engine.listener.onText("取消的片段", false);
        session.cancelCapture();
        equal(false, session.hasPendingCapture());
        equal(false, session.canRecord());
        engine.listener.onText("迟到的尾句", true);
        engine.listener.onStopped();
        equal(saved, session.text());
        equal(saved, store.data.get(id).text);
        equal("", session.pendingText());
        equal(true, session.canRecord());
        stopped(session, engine, "确认态取消");
        session.cancelCapture();
        equal(saved, session.text());
        equal(saved, store.data.get(id).text);

        // Save failures retain an editable candidate; retry appends once to the original text.
        stopped(session, engine, "保存候选");
        store.failWrites = true;
        equal(false, session.confirmCapture());
        equal(true, session.reviewReady());
        equal(saved, session.text());
        equal(saved, store.data.get(id).text);
        equal("保存候选", session.pendingText());
        equal(false, session.confirmCapture());
        session.updatePendingText("修正后重试");
        store.failWrites = false;
        equal(true, session.confirmCapture());
        equal(saved + "\n修正后重试", session.text());
        equal(1, store.data.size());
        equal(id, context.prefs.getString("draft", ""));

        // Preference failure occurs before publishing any candidate text to SQLite.
        saved = session.text();
        stopped(session, engine, "指针写入失败");
        int writesBeforeFailure = store.writes;
        context.prefs.fail = true;
        equal(false, session.confirmCapture());
        equal(writesBeforeFailure, store.writes);
        equal(saved, store.data.get(id).text);
        context.prefs.fail = false;
        session.cancelCapture();
        equal(saved, session.text());

        // A cancelled DB failure cannot publish a candidate even for a brand-new note.
        Context newContext = new Context();
        VoiceNotebookSession newNote = session(newContext);
        OfflineVoiceEngine newEngine = ready(newNote);
        VoiceNoteStore newStore = VoiceNoteStore.last;
        stopped(newNote, newEngine, "新笔记候选");
        absent(newContext.prefs.getString("draft", null));
        equal(0, newStore.writes);
        newStore.failWrites = true;
        equal(false, newNote.confirmCapture());
        String retryId = newContext.prefs.getString("draft", "");
        equal(false, retryId.isEmpty());
        equal("", newNote.text());
        equal(0, newStore.data.size());
        equal(false, newNote.confirmCapture());
        equal(retryId, newContext.prefs.getString("draft", ""));
        newNote.cancelCapture();
        equal(0, newStore.data.size());
        equal("", newNote.text());
        newStore.failWrites = false;
        stopped(newNote, newEngine, "下一次录音");
        equal(true, newNote.confirmCapture());
        equal(1, newStore.data.size());
        equal("下一次录音", newNote.text());

        // Rotation/background closes the engine after its tail, retaining only memory review.
        int[] oldUpdates = {0};
        VoiceNotebookSession.Observer first = refresh -> oldUpdates[0]++;
        session.attach(first);
        session.startRecording();
        engine.listener.onText("旋转前", false);
        session.detach(first);
        int updatesAtDetach = oldUpdates[0];
        equal(true, engine.closing);
        equal(0, store.closeCount);
        session.attach(refresh -> { });
        equal(false, session.canRecord());
        engine.listener.onText("旋转后的尾句", true);
        engine.listener.onStopped();
        equal(saved, session.text());
        equal("旋转后的尾句", session.pendingText());
        equal(saved, store.data.get(id).text);
        equal(updatesAtDetach, oldUpdates[0]);
        engine.listener.onClosed();
        equal(1, store.closeCount);
        equal(false, engine == OfflineVoiceEngine.last);
        engine = OfflineVoiceEngine.last;
        engine.listener.onReady();
        equal(true, session.reviewReady());
        equal(false, session.canRecord());
        equal("旋转后的尾句", session.pendingText());
        VoiceNotebookSession.Observer reviewScreen = refresh -> { };
        session.attach(reviewScreen);
        session.detach(reviewScreen);
        engine.listener.onClosed();
        equal("旋转后的尾句", session.pendingText());
        equal(2, store.closeCount);
        session.attach(reviewScreen);
        engine = OfflineVoiceEngine.last;
        engine.listener.onReady();
        equal(true, session.reviewReady());
        equal(true, session.confirmCapture());
        equal(saved + "\n旋转后的尾句", session.text());

        // Recreate the process owner over persisted IO: unconfirmed text is absent.
        saved = session.text();
        stopped(session, engine, "进程内临时片段");
        VoiceNotebookSession recreated = session(context);
        ready(recreated);
        equal(saved, recreated.text());
        equal(false, recreated.hasPendingCapture());
        session.cancelCapture();
        engine.listener.onText("取消后的意外回调", true);
        equal(saved, session.text());

        // Empty recognition can be edited or cancelled without creating an empty saved note.
        VoiceNotebookSession empty = session(new Context());
        OfflineVoiceEngine emptyEngine = ready(empty);
        VoiceNoteStore emptyStore = VoiceNoteStore.last;
        stopped(empty, emptyEngine, "");
        equal(false, empty.canConfirmCapture());
        equal(false, empty.confirmCapture());
        equal(0, emptyStore.data.size());
        empty.updatePendingText("人工修正空识别结果");
        equal(true, empty.confirmCapture());
        equal(1, emptyStore.data.size());
        equal("人工修正空识别结果", empty.text());

        // Recognition errors preserve already recognized text for manual review.
        stopped(session, engine, "错误前的候选");
        session.cancelCapture();
        session.startRecording();
        engine.listener.onText("仍可确认", false);
        engine.listener.onError("麦克风异常");
        session.stopRecording();
        engine.listener.onText("仍可确认的尾句", true);
        engine.listener.onStopped();
        equal(true, session.reviewReady());
        equal("仍可确认的尾句", session.pendingText());
        equal(saved, session.text());
        session.cancelCapture();

        // Existing manual autosave and fault recovery stay independent of capture.
        store.failWrites = true;
        session.updateText("尚未保存的新文字");
        absent(session.shareNote(id));
        absent(session.shareDay(original.day));
        equal("尚未保存的新文字", session.text());
        store.failWrites = false;
        session.edit(id);
        equal("尚未保存的新文字", session.text());
        equal(true, session.shareNote(id).endsWith("尚未保存的新文字"));
        store.failDeletes = true;
        session.delete(id);
        equal(1, store.data.size());
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

        // Failed draft reads are retriable without replacing valid persisted text.
        Context recovery = new Context();
        recovery.failReads = true;
        recovery.prefs.data.put("draft", "existing");
        recovery.notes.put("existing", new VoiceNote("existing", "2026-10-15", 1L, "磁盘上的原文"));
        VoiceNotebookSession recovered = session(recovery);
        ready(recovered);
        equal(false, recovered.draftLoaded());
        recovered.updateText("不得覆盖原文");
        equal("磁盘上的原文", recovery.notes.get("existing").text);
        VoiceNoteStore.last.failReads = false;
        recovered.finishNote();
        equal(true, recovered.draftLoaded());
        equal("磁盘上的原文", recovered.text());

        // File-picker delivery before onStart resumes import after old engine closing.
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
        System.out.println("Passed " + checks + " voice session capture/fault/lifecycle checks (platform IO doubles)");
    }
}
