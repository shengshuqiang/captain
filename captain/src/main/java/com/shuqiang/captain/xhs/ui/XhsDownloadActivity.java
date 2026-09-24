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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.BasePermissionActivity;
import com.shuqiang.captain.xhs.download.XhsDownloadContract;
import com.shuqiang.captain.xhs.download.XhsDownloadProgressStore;
import com.shuqiang.captain.xhs.download.RuntimeMediaSessionStore;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.model.XhsRequestMode;
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
    private static final String TAG = "XhsDownloadActivity";
    private static final int CLIPBOARD_HINT_MAX_LENGTH = 80;

    private enum UiState {
        IDLE,
        PARSING,
        MANUAL_BROWSING,
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
    private TextView selectionSummaryView;
    private TextView selectAllButton;
    private TextView secondaryActionView;
    private Button parseButton;
    private Button saveButton;
    private Button mobileRetryButton;
    private Button manualWebViewButton;
    private RecyclerView mediaListView;
    private View resourceWebViewContainer;
    private TextView resourceWebViewStatusView;
    private WebView resourceWebView;
    private ManualWebExtractionController manualWebExtractionController;

    private UiState uiState = UiState.IDLE;
    private XhsParseResult currentParseResult;
    private XhsMediaAdapter mediaAdapter;
    private ClipboardManager clipboardManager;
    private String lastSavedUri;
    private String lastAttemptedInputText;
    private String lastClipboardSnapshotAtParse;
    private boolean autoParsePending;
    private int parseRequestVersion;
    private boolean runtimeSessionHandedOff;

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
        if (manualWebExtractionController != null && manualWebExtractionController.isActive()) {
            manualWebExtractionController.close();
        }
        handleIncomingIntent(intent, true);
        if (autoParsePending) {
            autoParsePending = false;
            inputView.post(new Runnable() {
                @Override
                public void run() {
                    startParse("auto_parse_new_intent");
                }
            });
        }
    }

    private void initViews() {
        mediaListView = findViewById(R.id.media_list);
        mediaListView.setLayoutManager(new LinearLayoutManager(this));
        View header = getLayoutInflater().inflate(R.layout.item_xhs_download_header, mediaListView, false);
        inputView = header.findViewById(R.id.input_view);
        statusText = header.findViewById(R.id.status_text);
        statusProgress = header.findViewById(R.id.status_progress);
        resultContainer = header.findViewById(R.id.result_container);
        metaTypeView = header.findViewById(R.id.meta_type);
        metaTitleView = header.findViewById(R.id.meta_title);
        metaAuthorView = header.findViewById(R.id.meta_author);
        metaSummaryView = header.findViewById(R.id.meta_summary);
        selectionSummaryView = header.findViewById(R.id.selection_summary);
        selectAllButton = header.findViewById(R.id.select_all_button);
        secondaryActionView = findViewById(R.id.secondary_action);
        parseButton = header.findViewById(R.id.parse_button);
        saveButton = findViewById(R.id.save_button);
        mobileRetryButton = header.findViewById(R.id.mobile_retry_button);
        manualWebViewButton = header.findViewById(R.id.manual_webview_button);
        mediaListView = findViewById(R.id.media_list);
        resourceWebViewContainer = findViewById(R.id.resource_webview_container);
        resourceWebViewStatusView = findViewById(R.id.resource_webview_status);
        resourceWebView = findViewById(R.id.resource_sniff_webview);
        manualWebExtractionController = new ManualWebExtractionController(
                this,
                resourceWebViewContainer,
                resourceWebViewStatusView,
                (Button) findViewById(R.id.resource_webview_close),
                (Button) findViewById(R.id.resource_webview_refresh),
                (Button) findViewById(R.id.resource_webview_extract),
                resourceWebView,
                new ManualWebExtractionController.Listener() {
                    @Override
                    public void onExtractionReady(XhsParseResult result) {
                        updateLiveResources(result);
                        findViewById(R.id.download_content).bringToFront();
                        findViewById(R.id.return_webpage_button).setVisibility(View.VISIBLE);
                        setUiState(UiState.PARSE_SUCCESS, "资源持续更新，选择后保存；返回网页可继续播放。");
                    }

                    @Override
                    public void onResourcesUpdated(XhsParseResult result) {
                        updateLiveResources(result);
                    }

                    @Override
                    public boolean canUpdateResources() {
                        return uiState != UiState.SAVING;
                    }

                    @Override
                    public void onPageShown() {
                        if (uiState != UiState.SAVING) {
                            setUiState(UiState.MANUAL_BROWSING, "继续浏览网页并发现资源。");
                        }
                    }

                    @Override
                    public void onClosed() {
                        findViewById(R.id.return_webpage_button).setVisibility(View.GONE);
                        if (uiState != UiState.SAVING) restoreUiAfterManualBrowsing();
                    }
                }
        );
        findViewById(R.id.return_webpage_button).setOnClickListener(v -> manualWebExtractionController.showPage());
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
        mediaListView.setAdapter(new ConcatAdapter(new XhsDownloadHeaderAdapter(header), mediaAdapter));
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
        mobileRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startParse("mobile_retry", XhsRequestMode.MOBILE_RETRY);
            }
        });
        manualWebViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManualWebExtraction();
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
        if (!hasFocus || isManualBrowsing()) {
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
        String clipboardText = normalizeInput(readClipboardText());
        if (shouldOfferClipboardPrimaryAction(currentInput, clipboardText)) {
            parseButton.setText(R.string.xhs_download_paste_and_parse);
            inputView.setHint(currentInput.isEmpty()
                    ? buildClipboardHint(clipboardText)
                    : getString(R.string.xhs_download_input_hint));
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
        return hasResolvableClipboardContent(readClipboardText());
    }

    private boolean shouldUseClipboardPrimaryAction() {
        String currentInput = normalizeInput(inputView.getText().toString());
        String clipboardText = normalizeInput(readClipboardText());
        return shouldOfferClipboardPrimaryAction(currentInput, clipboardText);
    }

    /**
     * 当输入框仍停留在上次解析内容、而剪贴板出现新的可解析链接时，主按钮切回“粘贴&解析”。
     */
    private boolean shouldOfferClipboardPrimaryAction(String currentInput, String clipboardText) {
        if (!hasResolvableClipboardContent(clipboardText)) {
            return false;
        }
        if (currentInput.isEmpty()) {
            return true;
        }
        return !TextUtils.isEmpty(lastAttemptedInputText)
                && TextUtils.equals(currentInput, lastAttemptedInputText)
                && !TextUtils.equals(clipboardText, lastClipboardSnapshotAtParse);
    }

    private boolean hasResolvableClipboardContent(String clipboardText) {
        return extractUrlFromText(clipboardText) != null;
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
        if (autoParsePending || isManualBrowsing() || currentParseResult != null || !inputView.isEnabled()) {
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
        startParse(rawEntrySource, XhsRequestMode.AUTO);
    }

    private void startParse(String rawEntrySource, final XhsRequestMode requestMode) {
        if (uiState == UiState.SAVING) {
            Toast.makeText(this, "正在保存资源，请稍后再解析", Toast.LENGTH_SHORT).show();
            return;
        }
        final String rawInput = inputView.getText().toString().trim();
        if (rawInput.isEmpty()) {
            Toast.makeText(this, "请先粘贴网页分享文案或链接", Toast.LENGTH_SHORT).show();
            return;
        }
        final int requestVersion = ++parseRequestVersion;
        discardCurrentRuntimeSession();
        currentParseResult = null;
        mediaAdapter.setItems(null);
        resultContainer.setVisibility(View.GONE);
        if (manualWebExtractionController != null && manualWebExtractionController.isActive()) {
            manualWebExtractionController.close();
        }
        lastAttemptedInputText = normalizeInput(rawInput);
        // 记录本次解析开始时的剪贴板基线，后续只在出现“新剪贴板内容”时切回粘贴主动作。
        lastClipboardSnapshotAtParse = normalizeInput(readClipboardText());
        setUiState(UiState.PARSING, getString(R.string.xhs_download_parsing_status));
        final String entrySource = Intent.ACTION_SEND.equals(getIntent().getAction())
                ? "share_intent"
                : rawEntrySource;
        final String extractedUrl = extractUrlFromText(rawInput);
        parseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final XhsParseResult parseResult = parseRepository.parse(rawInput, entrySource, requestMode);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (requestVersion != parseRequestVersion) {
                                return;
                            }
                            applyParseResult(parseResult);
                        }
                    });
                } catch (final XhsParserException parserException) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (requestVersion != parseRequestVersion) {
                                return;
                            }
                            if (shouldOfferManualWebExtraction(parserException, extractedUrl)) {
                                startManualWebExtraction(extractedUrl, entrySource);
                                return;
                            }
                            applyParseError(parserException);
                        }
                    });
                } catch (Exception exception) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (requestVersion != parseRequestVersion) {
                                return;
                            }
                            applyParseError(new XhsParserException(XhsParseError.NETWORK_ERROR));
                        }
                    });
                }
            }
        });
    }

    private boolean shouldOfferManualWebExtraction(XhsParserException parserException, String extractedUrl) {
        return parserException.getParseError() == XhsParseError.NO_MEDIA_FOUND
                && com.shuqiang.captain.xhs.parser.WebViewResourceSniffer.canSniff(extractedUrl);
    }

    private void startManualWebExtraction() {
        String pageUrl = extractUrlFromText(inputView.getText().toString());
        if (TextUtils.isEmpty(pageUrl)) {
            Toast.makeText(this, "请先粘贴网页链接", Toast.LENGTH_SHORT).show();
            return;
        }
        startManualWebExtraction(pageUrl, "manual_webview");
    }

    private void startManualWebExtraction(String pageUrl, String entrySource) {
        if (uiState == UiState.SAVING) {
            Toast.makeText(this, "正在保存资源，请稍后再打开网页", Toast.LENGTH_SHORT).show();
            return;
        }
        // 手动浏览接管页面后，之前静态解析的迟到结果不得重新打开旧链接。
        parseRequestVersion++;
        inputView.clearFocus();
        setUiState(UiState.MANUAL_BROWSING, "正常浏览网页，点击查看资源后按需选择保存。");
        manualWebExtractionController.open(pageUrl, entrySource);
    }

    private boolean isManualBrowsing() {
        return manualWebExtractionController != null && manualWebExtractionController.isActive();
    }

    private void restoreUiAfterManualBrowsing() {
        if (currentParseResult != null) {
            setUiState(UiState.PARSE_SUCCESS, "已关闭网页，保留上一次解析结果。");
        } else {
            setUiState(UiState.IDLE, "已关闭网页，可重新解析或再次手动浏览。");
        }
    }

    /** 更新发现结果不触发下载，也不重置同一页面上的用户选择。 */
    private void updateLiveResources(XhsParseResult result) {
        if (uiState == UiState.SAVING) return;
        java.util.Set<String> selectedUrls = new java.util.HashSet<>();
        if (result != null && currentParseResult != null
                && TextUtils.equals(result.getPageUrl(), currentParseResult.getPageUrl())) {
            for (XhsMediaItem item : currentParseResult.getSelectedItems()) selectedUrls.add(item.getMediaUrl());
        }
        discardCurrentRuntimeSession();
        currentParseResult = result;
        runtimeSessionHandedOff = false;
        resultContainer.setVisibility(View.VISIBLE);
        if (result == null) {
            mediaAdapter.setItems(null);
            metaTypeView.setText("发现资源 0 项");
            metaTitleView.setText("等待网页中的媒体资源");
            metaAuthorView.setText("网页与嗅探仍在继续");
        } else {
            for (XhsMediaItem item : result.getMediaItems()) item.setSelected(selectedUrls.contains(item.getMediaUrl()));
            metaTypeView.setText("发现资源 " + result.getMediaCount() + " 项");
            metaTitleView.setText(result.getDisplayTitle());
            metaAuthorView.setText("来源：" + result.getAuthorName());
            mediaAdapter.setItems(result.getMediaItems());
        }
        metaSummaryView.setText("仅展示来源，默认不下载。广告属性与流是否可分离尚未确认；部分 blob 或跨域播放器无法直接关联。");
        refreshSelectionSummary();
    }

    private void applyParseResult(XhsParseResult parseResult) {
        discardCurrentRuntimeSession();
        currentParseResult = parseResult;
        runtimeSessionHandedOff = false;
        lastSavedUri = null;
        XhsDownloadProgressStore.clear();
        secondaryActionView.setVisibility(View.GONE);
        resultContainer.setVisibility(View.VISIBLE);
        metaTypeView.setText(parseResult.getPrimaryMediaType().getDisplayName() + " · " + parseResult.getMediaCount() + " 项");
        metaTitleView.setText(parseResult.getDisplayTitle());
        metaAuthorView.setText("来源：" + (TextUtils.isEmpty(parseResult.getAuthorName()) ? "未知站点" : parseResult.getAuthorName()));
        metaSummaryView.setText("来源：" + parseResult.getParseStrategy());
        // 发现资源不代表下载意愿；只有用户明确选择后才允许保存。
        for (XhsMediaItem item : parseResult.getMediaItems()) {
            item.setSelected(false);
        }
        mediaAdapter.setItems(parseResult.getMediaItems());
        refreshSelectionSummary();
        setUiState(UiState.PARSE_SUCCESS, "已发现资源，仅展示来源信息；选择需要的项目后保存。");
    }

    private void applyParseError(XhsParserException parserException) {
        Log.w(TAG, "parse failed, error=" + parserException.getParseError()
                + ", inputUrl=" + summarizeUrlForLog(extractUrlFromText(lastAttemptedInputText)));
        discardCurrentRuntimeSession();
        currentParseResult = null;
        XhsDownloadProgressStore.clear();
        mediaAdapter.setItems(null);
        resultContainer.setVisibility(View.GONE);
        secondaryActionView.setVisibility(View.GONE);
        setUiState(UiState.PARSE_FAILED, parserException.getMessage());
    }

    private String summarizeUrlForLog(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return "empty";
        }
        try {
            Uri uri = Uri.parse(rawUrl);
            String id = uri.getQueryParameter("id");
            String host = uri.getHost();
            return (TextUtils.isEmpty(host) ? "unknown" : host)
                    + (TextUtils.isEmpty(id) ? "" : "?id=" + id);
        } catch (Exception ignored) {
            return "invalid";
        }
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
        mediaAdapter.notifySelectionChanged();
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
        mediaListView.setVisibility(targetState == UiState.MANUAL_BROWSING ? View.GONE : View.VISIBLE);
        statusText.setText(message);
        boolean saving = targetState == UiState.SAVING;
        boolean browsing = targetState == UiState.MANUAL_BROWSING;
        boolean controlsLocked = saving || browsing;
        inputView.setEnabled(!controlsLocked);
        selectAllButton.setEnabled(!controlsLocked);
        mobileRetryButton.setEnabled(!controlsLocked && targetState != UiState.PARSING);
        manualWebViewButton.setEnabled(!controlsLocked && targetState != UiState.PARSING);
        switch (targetState) {
            case PARSING:
            case SAVING:
                statusProgress.setVisibility(View.VISIBLE);
                parseButton.setEnabled(false);
                saveButton.setEnabled(false);
                break;
            case MANUAL_BROWSING:
                statusProgress.setVisibility(View.GONE);
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
        try {
            Intent serviceIntent = XhsDownloadContract.buildStartIntent(this, currentParseResult);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            runtimeSessionHandedOff = true;
        } catch (RuntimeException exception) {
            setUiState(UiState.PARSE_SUCCESS, "启动保存任务失败，请稍后重试。");
            Toast.makeText(this, "启动保存任务失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void applySaveSummary(XhsSaveSummary saveSummary) {
        lastSavedUri = saveSummary.getLastSavedUri();
        String summaryMessage = saveSummary.getCurrentMessage()
                + " · 成功 " + saveSummary.getSuccessCount()
                + "，重复 " + saveSummary.getDuplicateCount()
                + "，失败 " + saveSummary.getFailedCount();
        if (saveSummary.isComplete()) {
            // 运行态会话只允许 Service 领取一次，完成后禁用旧句柄，避免无效重复保存。
            if (hasRuntimeMediaItems()) {
                updateAllSelection(false);
            }
            if (saveSummary.getFailedCount() == 0 && saveSummary.hasAnySuccess()) {
                setUiState(UiState.SAVE_SUCCESS, summaryMessage);
            } else if (saveSummary.isPartialSuccess()) {
                setUiState(UiState.SAVE_PARTIAL_SUCCESS, summaryMessage);
            } else {
                setUiState(UiState.PARSE_SUCCESS, summaryMessage);
            }
            manualWebExtractionController.refreshList();
            secondaryActionView.setVisibility(saveSummary.hasAnySuccess() ? View.VISIBLE : View.GONE);
        } else {
            setUiState(UiState.SAVING, summaryMessage);
        }
    }

    private boolean hasRuntimeMediaItems() {
        if (currentParseResult == null || currentParseResult.getMediaItems() == null) {
            return false;
        }
        for (XhsMediaItem item : currentParseResult.getMediaItems()) {
            if (item != null && item.requiresRuntimeSession()) {
                return true;
            }
        }
        return false;
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
        XhsMediaItem previewItem = currentParseResult.getMediaItems().get(position);
        if (previewItem.requiresSourceResolution() && !previewItem.requiresRuntimeSession()) {
            String message = previewItem.getMediaType() == XhsMediaType.VIDEO
                    ? "短效视频会在保存时取源；保存为 MP4 后可播放"
                    : "该资源依赖当前浏览会话，请保存后查看";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.ArrayList<XhsMediaItem> previewItems = new java.util.ArrayList<>();
        previewItems.add(previewItem);
        XhsParseResult singlePreview = new XhsParseResult(currentParseResult.getNoteId(),
                currentParseResult.getPageUrl(), currentParseResult.getCanonicalUrl(),
                currentParseResult.getAuthorName(), currentParseResult.getTitle(),
                previewItem.getCoverUrl(), currentParseResult.getParseStrategy(),
                currentParseResult.getEntrySource(), previewItems);
        if (previewItem.requiresRuntimeSession()) {
            singlePreview = manualWebExtractionController.registerPreview(singlePreview);
            if (singlePreview == null) {
                Toast.makeText(this, "请重新打开网页后预览", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        startActivity(XhsPreviewActivity.buildIntent(this, singlePreview, 0));
    }

    @Override
    public void onBackPressed() {
        if (manualWebExtractionController != null
                && manualWebExtractionController.handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (manualWebExtractionController != null) {
            manualWebExtractionController.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (manualWebExtractionController != null) {
            manualWebExtractionController.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        parseExecutor.shutdownNow();
        if (!runtimeSessionHandedOff) {
            discardCurrentRuntimeSession();
        }
        if (manualWebExtractionController != null) {
            manualWebExtractionController.destroy();
        }
        super.onDestroy();
    }

    private void discardCurrentRuntimeSession() {
        if (runtimeSessionHandedOff || currentParseResult == null
                || currentParseResult.getMediaItems() == null) {
            return;
        }
        for (XhsMediaItem item : currentParseResult.getMediaItems()) {
            if (item != null && item.requiresRuntimeSession()) {
                RuntimeMediaSessionStore.getInstance().discard(item.getRuntimeSessionId());
                break;
            }
        }
        runtimeSessionHandedOff = false;
    }
}
