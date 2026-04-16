package com.shuqiang.captain.xhs.ui;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.captain.base.BasePermissionActivity;
import com.shuqiang.captain.xhs.download.XhsDownloadContract;
import com.shuqiang.captain.xhs.download.XhsDownloadProgressStore;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.parser.XhsParseRepository;
import com.shuqiang.captain.xhs.parser.XhsParserException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

import captain.R;

/**
 * 小红书公开内容下载页，负责输入、预览和保存动作编排。
 */
public class XhsDownloadActivity extends BasePermissionActivity {
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    private static final int CLIPBOARD_HINT_MAX_LENGTH = 80;

    private enum UiState {
        IDLE,
        PARSING,
        PARSE_SUCCESS,
        PARSE_FAILED,
        SAVING,
        SAVE_PARTIAL_SUCCESS,
        SAVE_SUCCESS
    }

    private final ExecutorService parseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final XhsParseRepository parseRepository = new XhsParseRepository();
    private final ClipboardManager.OnPrimaryClipChangedListener primaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    refreshPrimaryParseAction();
                }
            };

    private EditText inputView;
    private TextView statusText;
    private ProgressBar statusProgress;
    private View resultContainer;
    private TextView metaTypeView;
    private TextView metaTitleView;
    private TextView metaAuthorView;
    private TextView metaSummaryView;
    private ImageView coverImageView;
    private TextView selectionSummaryView;
    private TextView selectAllButton;
    private TextView secondaryActionView;
    private Button parseButton;
    private Button saveButton;
    private RecyclerView mediaListView;

    private UiState uiState = UiState.IDLE;
    private XhsParseResult currentParseResult;
    private XhsMediaAdapter mediaAdapter;
    private ClipboardManager clipboardManager;
    private String lastSavedUri;
    private String lastAttemptedInputText;
    private boolean autoParsePending;

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            XhsSaveSummary saveSummary = (XhsSaveSummary) intent.getSerializableExtra(XhsDownloadContract.EXTRA_SAVE_SUMMARY);
            if (saveSummary != null) {
                applySaveSummary(saveSummary);
            }
        }
    };

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_xhs_download;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.xhs_download_page_title);
        initViews();
        handleIncomingIntent(getIntent(), true);
        setUiState(UiState.IDLE, getString(R.string.xhs_download_idle_status));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent, true);
    }

    private void initViews() {
        inputView = findViewById(R.id.input_view);
        statusText = findViewById(R.id.status_text);
        statusProgress = findViewById(R.id.status_progress);
        resultContainer = findViewById(R.id.result_container);
        metaTypeView = findViewById(R.id.meta_type);
        metaTitleView = findViewById(R.id.meta_title);
        metaAuthorView = findViewById(R.id.meta_author);
        metaSummaryView = findViewById(R.id.meta_summary);
        coverImageView = findViewById(R.id.cover_image);
        selectionSummaryView = findViewById(R.id.selection_summary);
        selectAllButton = findViewById(R.id.select_all_button);
        secondaryActionView = findViewById(R.id.secondary_action);
        parseButton = findViewById(R.id.parse_button);
        saveButton = findViewById(R.id.save_button);
        mediaListView = findViewById(R.id.media_list);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        mediaAdapter = new XhsMediaAdapter(new XhsMediaAdapter.OnMediaActionListener() {
            @Override
            public void onSelectionChanged() {
                refreshSelectionSummary();
            }

            @Override
            public void onPreviewRequested(int position) {
                openPreview(position);
            }
        });
        mediaListView.setLayoutManager(new GridLayoutManager(this, 1));
        mediaListView.setAdapter(mediaAdapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable editable) {
                refreshPrimaryParseAction();
            }
        });
        parseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleParseAction();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSave();
            }
        });
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBulkSelection();
            }
        });
        secondaryActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSavedMedia();
            }
        });
        coverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPreview(0);
            }
        });
    }

    private void handleIncomingIntent(Intent intent, boolean allowAutoParse) {
        if (intent == null) {
            return;
        }
        String incomingText = extractIncomingText(intent);
        if (!TextUtils.isEmpty(incomingText)) {
            inputView.setText(incomingText);
            inputView.setSelection(incomingText.length());
            autoParsePending = allowAutoParse;
        }
    }

    /**
     * 分享入口优先聚合文本和剪贴板内容，兼容不同 App 的发送实现差异。
     */
    private String extractIncomingText(Intent intent) {
        String incomingText = intent.getStringExtra(EXTRA_INPUT_TEXT);
        if (!TextUtils.isEmpty(incomingText)) {
            return incomingText;
        }
        String action = intent.getAction();
        if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            return null;
        }
        CharSequence extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(extraText)) {
            return extraText.toString();
        }
        ClipData clipData = intent.getClipData();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < clipData.getItemCount(); i++) {
            CharSequence itemText = clipData.getItemAt(i).coerceToText(this);
            if (TextUtils.isEmpty(itemText)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(itemText);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(downloadReceiver, new IntentFilter(XhsDownloadContract.ACTION_PROGRESS));
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener);
        }
        if (uiState == UiState.SAVING) {
            XhsSaveSummary latestSummary = XhsDownloadProgressStore.getLatest();
            if (latestSummary != null) {
                applySaveSummary(latestSummary);
            }
        }
        if (autoParsePending) {
            autoParsePending = false;
            inputView.post(new Runnable() {
                @Override
                public void run() {
                    startParse("auto_parse");
                }
            });
        } else {
            inputView.post(new Runnable() {
                @Override
                public void run() {
                    refreshPrimaryParseAction();
                    focusInputIfNeeded();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener);
        }
        unregisterReceiver(downloadReceiver);
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            return;
        }
        refreshPrimaryParseAction();
        focusInputIfNeeded();
    }

    private String readClipboardText() {
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return null;
        }
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        CharSequence text = clipData.getItemAt(0).coerceToText(this);
        return TextUtils.isEmpty(text) ? null : text.toString();
    }

    private String extractUrlFromText(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(https?://[^\\s]+)").matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void refreshPrimaryParseAction() {
        String currentInput = normalizeInput(inputView.getText().toString());
        String clipboardText = readClipboardText();
        if (currentInput.isEmpty() && extractUrlFromText(clipboardText) != null) {
            parseButton.setText(R.string.xhs_download_paste_and_parse);
            inputView.setHint(buildClipboardHint(clipboardText));
            return;
        }
        inputView.setHint(R.string.xhs_download_input_hint);
        if (!currentInput.isEmpty() && !TextUtils.isEmpty(lastAttemptedInputText)
                && !TextUtils.equals(currentInput, lastAttemptedInputText)) {
            parseButton.setText(R.string.xhs_download_reparse);
            return;
        }
        parseButton.setText(R.string.xhs_download_parse);
    }

    private boolean hasResolvableClipboardContent() {
        return extractUrlFromText(readClipboardText()) != null;
    }

    private boolean shouldUseClipboardPrimaryAction() {
        return normalizeInput(inputView.getText().toString()).isEmpty() && hasResolvableClipboardContent();
    }

    private String normalizeInput(String rawText) {
        return rawText == null ? "" : rawText.trim();
    }

    private String buildClipboardHint(String clipboardText) {
        String normalizedHint = normalizeInput(clipboardText).replaceAll("\\s+", " ");
        if (normalizedHint.isEmpty()) {
            return getString(R.string.xhs_download_input_hint);
        }
        if (normalizedHint.length() <= CLIPBOARD_HINT_MAX_LENGTH) {
            return normalizedHint;
        }
        return normalizedHint.substring(0, CLIPBOARD_HINT_MAX_LENGTH - 3) + "...";
    }

    private void focusInputIfNeeded() {
        if (autoParsePending || currentParseResult != null || !inputView.isEnabled()) {
            return;
        }
        if (!inputView.isFocused()) {
            inputView.requestFocus();
        }
        inputView.setSelection(inputView.getText().length());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 优先按实际保存结果的 URI 推断类型，避免混合资源场景被主类型带偏。
     */
    private String resolveSavedMimeType(Uri uri) {
        try {
            String contentMimeType = getContentResolver().getType(uri);
            if (!TextUtils.isEmpty(contentMimeType)) {
                return contentMimeType;
            }
        } catch (Exception ignored) {
            // 降级到扩展名推断。
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (!TextUtils.isEmpty(extension)) {
            String mappedMimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase(Locale.US));
            if (!TextUtils.isEmpty(mappedMimeType)) {
                return mappedMimeType;
            }
        }
        if (currentParseResult != null && currentParseResult.getPrimaryMediaType() == XhsMediaType.VIDEO) {
            return "video/*";
        }
        if (currentParseResult != null && currentParseResult.getPrimaryMediaType() == XhsMediaType.PDF) {
            return "application/pdf";
        }
        return "image/*";
    }

    /**
     * 主按钮根据输入态在“粘贴并解析 / 解析 / 重新解析”之间切换。
     */
    private void handleParseAction() {
        if (shouldUseClipboardPrimaryAction()) {
            String clipboardText = readClipboardText();
            if (TextUtils.isEmpty(clipboardText) || extractUrlFromText(clipboardText) == null) {
                refreshPrimaryParseAction();
                Toast.makeText(this, "请先粘贴网页分享文案或链接", Toast.LENGTH_SHORT).show();
                return;
            }
            inputView.setText(clipboardText);
            inputView.setSelection(clipboardText.length());
            startParse("clipboard_primary");
            return;
        }
        startParse("manual_input");
    }

    private void startParse(String rawEntrySource) {
        if (uiState == UiState.SAVING) {
            Toast.makeText(this, "正在保存资源，请稍后再解析", Toast.LENGTH_SHORT).show();
            return;
        }
        final String rawInput = inputView.getText().toString().trim();
        if (rawInput.isEmpty()) {
            Toast.makeText(this, "请先粘贴网页分享文案或链接", Toast.LENGTH_SHORT).show();
            return;
        }
        lastAttemptedInputText = normalizeInput(rawInput);
        setUiState(UiState.PARSING, getString(R.string.xhs_download_parsing_status));
        final String entrySource = Intent.ACTION_SEND.equals(getIntent().getAction())
                ? "share_intent"
                : rawEntrySource;
        parseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final XhsParseResult parseResult = parseRepository.parse(rawInput, entrySource);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            applyParseResult(parseResult);
                        }
                    });
                } catch (final XhsParserException parserException) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            applyParseError(parserException);
                        }
                    });
                } catch (Exception exception) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            applyParseError(new XhsParserException(XhsParseError.NETWORK_ERROR));
                        }
                    });
                }
            }
        });
    }

    private void applyParseResult(XhsParseResult parseResult) {
        currentParseResult = parseResult;
        lastSavedUri = null;
        XhsDownloadProgressStore.clear();
        secondaryActionView.setVisibility(View.GONE);
        resultContainer.setVisibility(View.VISIBLE);
        metaTypeView.setText(parseResult.getPrimaryMediaType().getDisplayName() + " · " + parseResult.getMediaCount() + " 项");
        metaTitleView.setText(parseResult.getDisplayTitle());
        metaAuthorView.setText("来源：" + (TextUtils.isEmpty(parseResult.getAuthorName()) ? "未知站点" : parseResult.getAuthorName()));
        metaSummaryView.setText("来源：" + parseResult.getParseStrategy());
        if (parseResult.getPrimaryMediaType() == XhsMediaType.PDF) {
            coverImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            coverImageView.setImageResource(R.drawable.ic_file_save);
        } else {
            coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(parseResult.getCoverUrl())
                    .into(coverImageView);
        }

        int spanCount = parseResult.getPrimaryMediaType() == XhsMediaType.VIDEO ? 1 : 3;
        mediaListView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mediaAdapter.setItems(parseResult.getMediaItems());
        refreshSelectionSummary();
        setUiState(UiState.PARSE_SUCCESS, "解析完成，可勾选后保存图片、视频或 PDF。");
    }

    private void applyParseError(XhsParserException parserException) {
        currentParseResult = null;
        XhsDownloadProgressStore.clear();
        mediaAdapter.setItems(null);
        resultContainer.setVisibility(View.GONE);
        secondaryActionView.setVisibility(View.GONE);
        setUiState(UiState.PARSE_FAILED, parserException.getMessage());
    }

    private void toggleBulkSelection() {
        if (currentParseResult == null || currentParseResult.getMediaCount() <= 1) {
            return;
        }
        updateAllSelection(currentParseResult.getSelectedCount() < currentParseResult.getMediaCount());
    }

    private void updateAllSelection(boolean selected) {
        if (currentParseResult == null || currentParseResult.getMediaItems() == null) {
            return;
        }
        for (XhsMediaItem mediaItem : currentParseResult.getMediaItems()) {
            mediaItem.setSelected(selected);
        }
        mediaAdapter.notifyDataSetChanged();
        refreshSelectionSummary();
    }

    private void refreshSelectionSummary() {
        if (currentParseResult == null) {
            selectionSummaryView.setText("已选择 0 / 0 项");
            selectAllButton.setVisibility(View.GONE);
            saveButton.setText(getString(R.string.xhs_download_save_default));
            saveButton.setEnabled(false);
            return;
        }
        int selectedCount = currentParseResult.getSelectedCount();
        int totalCount = currentParseResult.getMediaCount();
        selectionSummaryView.setText("已选择 " + selectedCount + " / " + totalCount + " 项");
        boolean showBulkToggle = totalCount > 1;
        selectAllButton.setVisibility(showBulkToggle ? View.VISIBLE : View.GONE);
        if (showBulkToggle) {
            selectAllButton.setText(selectedCount == totalCount
                    ? R.string.xhs_download_select_none
                    : R.string.xhs_download_select_all);
        }
        saveButton.setText("保存 " + selectedCount + " 项");
        saveButton.setEnabled(selectedCount > 0 && uiState != UiState.SAVING);
    }

    private void setUiState(UiState targetState, String message) {
        this.uiState = targetState;
        statusText.setText(message);
        boolean saving = targetState == UiState.SAVING;
        inputView.setEnabled(!saving);
        selectAllButton.setEnabled(!saving);
        switch (targetState) {
            case PARSING:
            case SAVING:
                statusProgress.setVisibility(View.VISIBLE);
                parseButton.setEnabled(false);
                saveButton.setEnabled(false);
                break;
            case SAVE_SUCCESS:
            case SAVE_PARTIAL_SUCCESS:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                refreshSelectionSummary();
                saveButton.setEnabled(false);
                break;
            case PARSE_SUCCESS:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                refreshSelectionSummary();
                break;
            case PARSE_FAILED:
            case IDLE:
            default:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                refreshSelectionSummary();
                break;
        }
        refreshPrimaryParseAction();
    }

    private void startSave() {
        if (currentParseResult == null || !currentParseResult.hasSelection()) {
            Toast.makeText(this, "请至少勾选一个资源", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            handleRequstPermissionAndReadWriteFile();
            return;
        }
        startSaveService();
    }

    @Override
    public void onReadWriteFile() {
        startSaveService();
    }

    private void startSaveService() {
        XhsDownloadProgressStore.clear();
        lastSavedUri = null;
        secondaryActionView.setVisibility(View.GONE);
        setUiState(UiState.SAVING, "正在准备保存资源…");
        Intent serviceIntent = XhsDownloadContract.buildStartIntent(this, currentParseResult);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void applySaveSummary(XhsSaveSummary saveSummary) {
        lastSavedUri = saveSummary.getLastSavedUri();
        String summaryMessage = saveSummary.getCurrentMessage()
                + " · 成功 " + saveSummary.getSuccessCount()
                + "，重复 " + saveSummary.getDuplicateCount()
                + "，失败 " + saveSummary.getFailedCount();
        if (saveSummary.isComplete()) {
            if (saveSummary.getFailedCount() == 0 && saveSummary.hasAnySuccess()) {
                setUiState(UiState.SAVE_SUCCESS, summaryMessage);
            } else if (saveSummary.isPartialSuccess()) {
                setUiState(UiState.SAVE_PARTIAL_SUCCESS, summaryMessage);
            } else {
                setUiState(UiState.PARSE_SUCCESS, summaryMessage);
            }
            secondaryActionView.setVisibility(saveSummary.hasAnySuccess() ? View.VISIBLE : View.GONE);
        } else {
            setUiState(UiState.SAVING, summaryMessage);
        }
    }

    private void openSavedMedia() {
        if (!TextUtils.isEmpty(lastSavedUri)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(lastSavedUri);
                String mimeType = resolveSavedMimeType(uri);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                return;
            } catch (Exception ignored) {
                // 降级到系统媒体入口。
            }
        }
        try {
            if (currentParseResult != null && currentParseResult.getPrimaryMediaType() == XhsMediaType.PDF) {
                Toast.makeText(this, "请通过上方已保存资源直接打开 PDF", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW,
                    currentParseResult != null && currentParseResult.getPrimaryMediaType() == XhsMediaType.VIDEO
                            ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(fallbackIntent);
        } catch (Exception e) {
            Toast.makeText(this, "未找到可打开的相册应用", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPreview(int position) {
        if (currentParseResult == null || currentParseResult.getMediaItems() == null
                || currentParseResult.getMediaItems().isEmpty()) {
            return;
        }
        if (position < 0 || position >= currentParseResult.getMediaItems().size()) {
            position = 0;
        }
        startActivity(XhsPreviewActivity.buildIntent(this, currentParseResult, position));
    }

    @Override
    protected void onDestroy() {
        parseExecutor.shutdownNow();
        super.onDestroy();
    }
}
