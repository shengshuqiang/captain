package com.shuqiang.captain.voice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.captain.base.BaseActivity;

import java.util.List;
import java.util.Map;

import captain.R;

/** Screen bindings only; the application session owns the draft and recording lifetime. */
public final class VoiceNotebookActivity extends BaseActivity implements VoiceNotebookSession.Observer {
    private static final int IMPORT_MODEL = 701, MICROPHONE = 702;
    private VoiceNotebookSession session;
    private EditText editor;
    private TextView status;
    private LinearLayout notes;
    private Button record, save, importButton;
    private boolean changing, foreground;

    @Override protected int getContentLayoutResource() { return R.layout.voice_notebook; }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("语音记事本");
        session = VoiceNotebookSession.get(this);
        editor = findViewById(R.id.voice_editor);
        // The session is the text owner; Android's stale view-state must not replace a final result.
        editor.setSaveEnabled(false);
        status = findViewById(R.id.voice_status);
        notes = findViewById(R.id.voice_notes);
        record = findViewById(R.id.voice_record);
        save = findViewById(R.id.voice_save);
        importButton = findViewById(R.id.voice_model_import);
        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!changing) session.updateText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        record.setOnClickListener(v -> {
            if (session.recording()) session.stopRecording();
            else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE);
            } else session.startRecording();
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

    @Override public void onChanged(boolean refreshNotes) {
        changing = true;
        if (!session.text().equals(editor.getText().toString())) {
            editor.setText(session.text());
            editor.setSelection(editor.length());
        }
        changing = false;
        status.setText(session.status());
        record.setEnabled(session.canRecord());
        record.setText(session.recording() ? "停止录音" : "开始录音");
        save.setEnabled(!session.busy());
        save.setText(session.draftLoaded() ? "完成 / 新建" : "重试读取");
        editor.setEnabled(session.draftLoaded() && !session.busy());
        importButton.setEnabled(session.canImport());
        if (refreshNotes) refresh();
    }

    private void refresh() {
        Map<String, List<VoiceNote>> groups = session.grouped();
        if (groups == null) return; // Keep visible rows when a transient database read fails.
        notes.removeAllViews();
        if (groups.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("还没有笔记。说一句话，记下今天的想法。");
            notes.addView(empty);
        }
        for (Map.Entry<String, List<VoiceNote>> group : groups.entrySet()) {
            Button heading = new Button(this);
            heading.setText(group.getKey() + " · " + group.getValue().size() + " 条 · 分享当天");
            heading.setOnClickListener(v -> {
                if (session.busy()) { showError("请先停止录音再分享"); return; }
                share(session.shareDay(group.getKey()));
            });
            notes.addView(heading);
            for (VoiceNote note : group.getValue()) {
                TextView row = new TextView(this);
                row.setText(note.text);
                row.setTextSize(16);
                row.setPadding(12, 16, 12, 16);
                row.setContentDescription("笔记：" + note.text + "。点击编辑、分享或删除");
                row.setOnClickListener(v -> actions(note));
                notes.addView(row);
            }
        }
    }

    private void actions(VoiceNote note) {
        if (session.busy()) { showError("请先停止录音"); return; }
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
        if (request == MICROPHONE) {
            if (grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED && foreground) {
                session.startRecording();
            } else if (grants.length == 0 || grants[0] != PackageManager.PERMISSION_GRANTED) {
                showError("录音需要麦克风权限；仍可输入文字记事。可在系统设置中授权。");
            }
        }
    }

    @Override protected void onStart() {
        super.onStart();
        foreground = true;
        session.attach(this);
    }

    @Override protected void onStop() {
        foreground = false;
        session.detach(this);
        super.onStop();
    }
}
