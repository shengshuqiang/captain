package com.shuqiang.captain.voice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.captain.base.BaseActivity;

import java.util.List;
import java.util.Map;

import captain.R;

/** Presentation and gesture bindings; the session owns formal and provisional text separately. */
public final class VoiceNotebookActivity extends BaseActivity implements VoiceNotebookSession.Observer {
    private static final int IMPORT_MODEL = 701, MICROPHONE = 702;
    private VoiceNotebookSession session;
    private VoiceCaptureOverlay capture;
    private VoiceRecordTouchListener hold;
    private EditText editor;
    private TextView status;
    private LinearLayout notes;
    private Button record, save, importButton;
    private boolean changing, foreground;

    @Override protected int getContentLayoutResource() { return R.layout.voice_notebook; }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("语音记事本");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        session = VoiceNotebookSession.get(this);
        editor = findViewById(R.id.voice_editor);
        editor.setSaveEnabled(false);
        status = findViewById(R.id.voice_status);
        notes = findViewById(R.id.voice_notes);
        record = findViewById(R.id.voice_record);
        save = findViewById(R.id.voice_save);
        importButton = findViewById(R.id.voice_model_import);
        capture = new VoiceCaptureOverlay(this, session);
        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!changing) session.updateText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        hold = new VoiceRecordTouchListener(record, new VoiceHoldGesture.Listener() {
            @Override public boolean onStart() { return startCapture(); }
            @Override public void onCancelSelection(boolean selected) { capture.selectCancel(selected); }
            @Override public void onRelease(boolean discard) {
                if (discard) session.cancelCapture();
                else session.stopRecording();
            }
        });
        record.setOnTouchListener(hold);
        record.setOnClickListener(v -> {
            AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
            // TalkBack users can start with an accessible click and finish with the overlay stop action.
            if (manager != null && manager.isTouchExplorationEnabled()) startCapture();
            else Toast.makeText(this, R.string.voice_hold_hint, Toast.LENGTH_SHORT).show();
        });
        save.setOnClickListener(v -> session.finishNote());
        findViewById(R.id.voice_model_download).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(
                        "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip")));
            } catch (ActivityNotFoundException e) { showError("请使用浏览器下载 Vosk 中文小模型 ZIP"); }
        });
        importButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            try { startActivityForResult(intent, IMPORT_MODEL); }
            catch (ActivityNotFoundException e) { showError("设备没有可用的文件选择器"); }
        });
    }

    private boolean startCapture() {
        if (!foreground || !hasWindowFocus() || !session.canRecord()) return false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE);
            return false;
        }
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(editor.getWindowToken(), 0);
        editor.clearFocus();
        session.startRecording();
        return session.recording();
    }

    @Override public void onChanged(boolean refreshNotes) {
        changing = true;
        if (!session.text().equals(editor.getText().toString())) {
            editor.setText(session.text());
            editor.setSelection(editor.length());
        }
        changing = false;
        status.setText(session.status());
        // Disabling the original target while held would prevent ACTION_UP from reaching its listener.
        record.setEnabled(session.canRecord() || session.recording());
        record.setText(R.string.voice_hold_to_record);
        save.setEnabled(!session.busy());
        save.setText(session.draftLoaded() ? R.string.voice_finish_note : R.string.voice_retry_read);
        editor.setEnabled(session.canEditDraft());
        importButton.setEnabled(session.canImport());
        if (foreground) capture.bind();
        else capture.suspend();
        if (refreshNotes) refresh();
    }

    private void refresh() {
        Map<String, List<VoiceNote>> groups = session.grouped();
        if (groups == null) return;
        notes.removeAllViews();
        if (groups.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setTextAppearance(this, R.style.CaptainText_Body);
            empty.setText(R.string.voice_archive_empty);
            notes.addView(empty);
        }
        for (Map.Entry<String, List<VoiceNote>> group : groups.entrySet()) {
            Button heading = (Button) getLayoutInflater().inflate(R.layout.voice_archive_day, notes, false);
            heading.setText(group.getKey() + "  ·  " + group.getValue().size() + " 条  ·  分享当天");
            heading.setOnClickListener(v -> {
                if (session.busy()) { showError("请先确认或取消本次录音"); return; }
                share(session.shareDay(group.getKey()));
            });
            notes.addView(heading);
            for (VoiceNote note : group.getValue()) {
                TextView row = (TextView) getLayoutInflater().inflate(R.layout.voice_archive_note, notes, false);
                row.setText(note.text);
                row.setContentDescription("笔记：" + note.text + "。点击编辑、分享或删除");
                row.setOnClickListener(v -> actions(note));
                notes.addView(row);
            }
        }
    }

    private void actions(VoiceNote note) {
        if (session.busy()) { showError("请先确认或取消本次录音"); return; }
        new AlertDialog.Builder(this).setTitle("笔记操作")
                .setItems(new String[]{"编辑", "分享到飞书 / 其他应用", "删除"}, (dialog, which) -> {
                    if (which == 0) session.edit(note.id);
                    else if (which == 1) share(session.shareNote(note.id));
                    else new AlertDialog.Builder(this).setMessage("删除这条本机笔记？")
                            .setNegativeButton("取消", null).setPositiveButton("删除",
                                    (d, w) -> session.delete(note.id)).show();
                }).show();
    }

    private void share(String text) {
        if (text == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "语音记事本")
                .putExtra(Intent.EXTRA_TEXT, text);
        try { startActivity(Intent.createChooser(intent, "分享到飞书（选择飞书）")); }
        catch (ActivityNotFoundException e) { showError("请先安装飞书或其他支持文本分享的应用"); }
    }

    private void showError(String message) {
        status.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == IMPORT_MODEL && result == RESULT_OK && data != null && data.getData() != null) {
            session.importModel(data.getData());
        }
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if (request == MICROPHONE && foreground) {
            // Permission approval never starts audio without a fresh hold.
            showError(getString(R.string.voice_permission_hint));
        }
    }

    @Override protected void onStart() {
        super.onStart();
        foreground = true;
        session.attach(this);
    }

    @Override protected void onResume() {
        super.onResume();
        foreground = true;
        onChanged(false);
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (hold == null || session == null) return;
        // A dialog or system shade can cover a resumed Activity without calling onPause.
        if (!focused) {
            hold.interrupt();
            session.stopRecording();
        } else if (foreground) onChanged(false);
    }

    @Override protected void onPause() {
        foreground = false;
        hold.interrupt();
        session.stopRecording();
        capture.suspend();
        super.onPause();
    }

    @Override protected void onStop() {
        foreground = false;
        hold.interrupt();
        capture.suspend();
        session.detach(this);
        super.onStop();
    }
}
