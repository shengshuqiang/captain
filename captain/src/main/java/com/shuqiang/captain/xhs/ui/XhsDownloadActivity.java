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
import android.text.TextUtils;
import android.view.View;
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
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.parser.XhsParseRepository;
import com.shuqiang.captain.xhs.parser.XhsParserException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import captain.R;

/**
 * 小红书公开内容下载页，负责输入、预览和保存动作编排。
 */
public class XhsDownloadActivity extends BasePermissionActivity {
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";

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

    private EditText inputView;
    private TextView statusText;
    private ProgressBar statusProgress;
    private View resultContainer;
    private TextView pasteButton;
    private TextView metaTypeView;
    private TextView metaTitleView;
    private TextView metaAuthorView;
    private TextView metaSummaryView;
    private ImageView coverImageView;
    private TextView selectionSummaryView;
    private TextView selectAllButton;
    private TextView clearSelectionButton;
    private TextView secondaryActionView;
    private TextView clearButton;
    private Button parseButton;
    private Button saveButton;
    private RecyclerView mediaListView;

    private UiState uiState = UiState.IDLE;
    private XhsParseResult currentParseResult;
    private XhsMediaAdapter mediaAdapter;
    private String lastSavedUri;
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
        pasteButton = findViewById(R.id.paste_button);
        metaTypeView = findViewById(R.id.meta_type);
        metaTitleView = findViewById(R.id.meta_title);
        metaAuthorView = findViewById(R.id.meta_author);
        metaSummaryView = findViewById(R.id.meta_summary);
        coverImageView = findViewById(R.id.cover_image);
        selectionSummaryView = findViewById(R.id.selection_summary);
        selectAllButton = findViewById(R.id.select_all_button);
        clearSelectionButton = findViewById(R.id.clear_selection_button);
        secondaryActionView = findViewById(R.id.secondary_action);
        clearButton = findViewById(R.id.clear_button);
        parseButton = findViewById(R.id.parse_button);
        saveButton = findViewById(R.id.save_button);
        mediaListView = findViewById(R.id.media_list);

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

        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteFromClipboard();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCurrentContent();
            }
        });
        parseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startParse(false);
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
                updateAllSelection(true);
            }
        });
        clearSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAllSelection(false);
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
        String incomingText = intent.getStringExtra(EXTRA_INPUT_TEXT);
        if (TextUtils.isEmpty(incomingText) && Intent.ACTION_SEND.equals(intent.getAction())) {
            incomingText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        if (!TextUtils.isEmpty(incomingText)) {
            inputView.setText(incomingText);
            inputView.setSelection(incomingText.length());
            autoParsePending = allowAutoParse;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(downloadReceiver, new IntentFilter(XhsDownloadContract.ACTION_PROGRESS));
        if (autoParsePending) {
            autoParsePending = false;
            inputView.post(new Runnable() {
                @Override
                public void run() {
                    startParse(true);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(downloadReceiver);
        super.onStop();
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            Toast.makeText(this, "剪贴板暂无内容", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            Toast.makeText(this, "剪贴板暂无内容", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence text = clipData.getItemAt(0).coerceToText(this);
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "剪贴板暂无可解析文本", Toast.LENGTH_SHORT).show();
            return;
        }
        inputView.setText(text);
        inputView.setSelection(text.length());
    }

    private void clearCurrentContent() {
        inputView.setText("");
        currentParseResult = null;
        lastSavedUri = null;
        mediaAdapter.setItems(null);
        resultContainer.setVisibility(View.GONE);
        secondaryActionView.setVisibility(View.GONE);
        setUiState(UiState.IDLE, getString(R.string.xhs_download_idle_status));
    }

    private void startParse(boolean fromAutoTrigger) {
        if (uiState == UiState.SAVING) {
            Toast.makeText(this, "正在保存资源，请稍后再解析", Toast.LENGTH_SHORT).show();
            return;
        }
        final String rawInput = inputView.getText().toString().trim();
        if (rawInput.isEmpty()) {
            Toast.makeText(this, "请先粘贴小红书分享文案", Toast.LENGTH_SHORT).show();
            return;
        }
        setUiState(UiState.PARSING, getString(R.string.xhs_download_parsing_status));
        final String entrySource = Intent.ACTION_SEND.equals(getIntent().getAction())
                ? "share_intent"
                : (fromAutoTrigger ? "auto_parse" : "manual_input");
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
        secondaryActionView.setVisibility(View.GONE);
        resultContainer.setVisibility(View.VISIBLE);
        metaTypeView.setText(parseResult.getPrimaryMediaType().getDisplayName() + " · " + parseResult.getMediaCount() + " 项");
        metaTitleView.setText(parseResult.getDisplayTitle());
        metaAuthorView.setText("作者：" + (TextUtils.isEmpty(parseResult.getAuthorName()) ? "未知" : parseResult.getAuthorName()));
        metaSummaryView.setText("来源：" + parseResult.getParseStrategy());
        Glide.with(this)
                .load(parseResult.getCoverUrl())
                .into(coverImageView);

        int spanCount = parseResult.getPrimaryMediaType() == XhsMediaType.VIDEO ? 1 : 3;
        mediaListView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mediaAdapter.setItems(parseResult.getMediaItems());
        refreshSelectionSummary();
        setUiState(UiState.PARSE_SUCCESS, "解析完成，可勾选后保存到系统相册。");
    }

    private void applyParseError(XhsParserException parserException) {
        currentParseResult = null;
        mediaAdapter.setItems(null);
        resultContainer.setVisibility(View.GONE);
        secondaryActionView.setVisibility(View.GONE);
        setUiState(UiState.PARSE_FAILED, parserException.getMessage());
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
            saveButton.setText(getString(R.string.xhs_download_save_default));
            saveButton.setEnabled(false);
            return;
        }
        int selectedCount = currentParseResult.getSelectedCount();
        int totalCount = currentParseResult.getMediaCount();
        selectionSummaryView.setText("已选择 " + selectedCount + " / " + totalCount + " 项");
        saveButton.setText("保存 " + selectedCount + " 项");
        saveButton.setEnabled(selectedCount > 0 && uiState != UiState.SAVING);
    }

    private void setUiState(UiState targetState, String message) {
        this.uiState = targetState;
        statusText.setText(message);
        boolean saving = targetState == UiState.SAVING;
        inputView.setEnabled(!saving);
        pasteButton.setEnabled(!saving);
        clearButton.setEnabled(!saving);
        selectAllButton.setEnabled(!saving);
        clearSelectionButton.setEnabled(!saving);
        switch (targetState) {
            case PARSING:
            case SAVING:
                statusProgress.setVisibility(View.VISIBLE);
                parseButton.setEnabled(false);
                saveButton.setEnabled(false);
                clearButton.setText(R.string.xhs_download_clear);
                break;
            case SAVE_SUCCESS:
            case SAVE_PARTIAL_SUCCESS:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                clearButton.setText(R.string.xhs_download_continue);
                refreshSelectionSummary();
                saveButton.setEnabled(false);
                break;
            case PARSE_SUCCESS:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                clearButton.setText(R.string.xhs_download_clear);
                refreshSelectionSummary();
                break;
            case PARSE_FAILED:
            case IDLE:
            default:
                statusProgress.setVisibility(View.GONE);
                parseButton.setEnabled(true);
                clearButton.setText(R.string.xhs_download_clear);
                refreshSelectionSummary();
                break;
        }
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
                String mimeType = currentParseResult != null && currentParseResult.getPrimaryMediaType() == XhsMediaType.VIDEO
                        ? "video/*"
                        : "image/*";
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                return;
            } catch (Exception ignored) {
                // 降级到系统媒体入口。
            }
        }
        try {
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
        startActivity(XhsPreviewActivity.buildIntent(this,
                new java.util.ArrayList<>(currentParseResult.getMediaItems()), position));
    }

    @Override
    protected void onDestroy() {
        parseExecutor.shutdownNow();
        super.onDestroy();
    }
}
