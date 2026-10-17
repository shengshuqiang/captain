package com.shuqiang.captain.xhs.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shuqiang.captain.xhs.download.RuntimeMediaSessionStore;
import com.shuqiang.captain.xhs.download.XhsDownloadContract;
import com.shuqiang.captain.xhs.download.XhsDownloadProgressStore;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.storage.XhsMediaSaver;

import java.util.ArrayList;
import android.webkit.CookieManager;
import com.shuqiang.captain.xhs.parser.XhsHttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.shuqiang.captain.xhs.playback.CaptainVideoPlayer;
import com.shuqiang.captain.xhs.playback.PreviewVideoPlayback;

import captain.R;

/**
 * 统一承接图片预览和视频播放，并复用下载能力提供单项保存。
 */
public class XhsPreviewActivity extends AppCompatActivity {
    private static final String TAG = "XhsPreviewActivity";
    private static final int REQUEST_WRITE_STORAGE = 3001;
    private static final String EXTRA_PARSE_RESULT = "extra_parse_result";
    private static final String EXTRA_POSITION = "extra_position";

    private PreviewVideoPlayback videoPlayback;
    private boolean serviceSaveInProgress;
    private String retainedRuntimeSessionId;
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final XhsMediaSaver mediaSaver = new XhsMediaSaver();

    private XhsParseResult parseResult;
    private ArrayList<XhsMediaItem> mediaItems;
    private ViewPager previewPager;
    private TextView indicatorView;
    private TextView saveView;
    private boolean saveInProgress;
    private boolean runtimeSessionHandedOff;
    private final BroadcastReceiver saveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (saveInProgress && serviceSaveInProgress) {
                XhsSaveSummary summary = (XhsSaveSummary) intent.getSerializableExtra(
                        XhsDownloadContract.EXTRA_SAVE_SUMMARY);
                if (summary != null) updateRuntimeSave(summary);
            }
        }
    };

    public static Intent buildIntent(Context context, XhsParseResult parseResult, int position) {
        Intent intent = new Intent(context, XhsPreviewActivity.class);
        intent.putExtra(EXTRA_PARSE_RESULT, parseResult);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xhs_preview);
        // 媒体画布保留深色，系统栏也使用深色，避免白色控制与浅色背景混合。
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility()
                        & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        parseResult = (XhsParseResult) getIntent().getSerializableExtra(EXTRA_PARSE_RESULT);
        mediaItems = parseResult == null ? null : parseResult.getMediaItems();
        int startPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        if (mediaItems == null || mediaItems.isEmpty()) {
            finish();
            return;
        }
        if (mediaItems.get(0).requiresRuntimeSession()) {
            String sessionId = mediaItems.get(0).getRuntimeSessionId();
            if (!RuntimeMediaSessionStore.getInstance().retainForPreview(sessionId)) {
                Toast.makeText(this, "浏览会话已失效，请返回网页重新查看", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            retainedRuntimeSessionId = sessionId;
        }
        if (startPosition < 0 || startPosition >= mediaItems.size()) {
            startPosition = 0;
        }

        previewPager = findViewById(R.id.preview_pager);
        indicatorView = findViewById(R.id.preview_indicator);
        saveView = findViewById(R.id.preview_save);
        findViewById(R.id.preview_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerSaveCurrentItem();
            }
        });

        videoPlayback = new PreviewVideoPlayback(this);
        previewPager.setAdapter(new PreviewPagerAdapter());
        previewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
                resumeCurrentVideo(position);
            }
        });
        previewPager.setCurrentItem(startPosition, false);
        updateIndicator(startPosition);
        previewPager.post(new Runnable() {
            @Override
            public void run() {
                resumeCurrentVideo(previewPager.getCurrentItem());
            }
        });
    }

    @Override
    protected void onPause() {
        pauseAllVideos();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(saveReceiver, new IntentFilter(XhsDownloadContract.ACTION_PROGRESS));
        if (saveInProgress && serviceSaveInProgress) {
            XhsSaveSummary latest = XhsDownloadProgressStore.getLatest();
            if (latest != null) updateRuntimeSave(latest);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(saveReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopAllVideos();
        if (!isChangingConfigurations() && !runtimeSessionHandedOff
                && mediaItems != null && !mediaItems.isEmpty()
                && mediaItems.get(0).requiresRuntimeSession()) {
            RuntimeMediaSessionStore.getInstance().discard(mediaItems.get(0).getRuntimeSessionId());
        }
        saveExecutor.shutdownNow();
        RuntimeMediaSessionStore.getInstance().releasePreview(retainedRuntimeSessionId);
        super.onDestroy();
    }

    private void updateIndicator(int position) {
        indicatorView.setText(getString(R.string.xhs_preview_indicator, position + 1, mediaItems.size()));
    }

    private void pauseAllVideos() {
        if (videoPlayback != null) videoPlayback.pause();
    }

    private void stopAllVideos() {
        if (videoPlayback != null) videoPlayback.close();
    }

    private void resumeCurrentVideo(int activePosition) {
        if (videoPlayback != null) videoPlayback.select(activePosition);
    }

    @Override
    public void onBackPressed() {
        if (videoPlayback == null || !videoPlayback.exitFullscreen()) super.onBackPressed();
    }

    private void triggerSaveCurrentItem() {
        if (saveInProgress || mediaItems == null || mediaItems.isEmpty()) {
            return;
        }
        XhsMediaItem currentItem = mediaItems.get(previewPager.getCurrentItem());
        if (currentItem.requiresRuntimeSession() && runtimeSessionHandedOff) {
            finish();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            return;
        }
        if (currentItem.getMediaType() == XhsMediaType.VIDEO || currentItem.requiresRuntimeSession()) {
            startRuntimeSave();
            return;
        }
        saveCurrentItem();
    }

    /** 预览视频沿用前台下载服务；浏览会话仍只移交一次，离开页面也能完成。 */
    private void startRuntimeSave() {
        XhsSaveSummary active = XhsDownloadProgressStore.getLatest();
        if (active != null && !active.isComplete()) {
            Toast.makeText(this, "已有保存任务进行中，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        // 预览页的按钮只保存当前条目，不能沿用清单里其它条目的勾选状态。
        int currentPosition = previewPager.getCurrentItem();
        for (int i = 0; i < mediaItems.size(); i++) {
            mediaItems.get(i).setSelected(i == currentPosition);
        }
        XhsDownloadProgressStore.clear();
        saveInProgress = true;
        serviceSaveInProgress = true;
        saveView.setEnabled(false);
        saveView.setText("正在保存所选资源…");
        String cacheSessionId = videoPlayback == null ? null : videoPlayback.reserveSave(currentPosition);
        try {
            Intent intent = XhsDownloadContract.buildStartIntent(this, parseResult, cacheSessionId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            runtimeSessionHandedOff = mediaItems.get(currentPosition).requiresRuntimeSession();
        } catch (RuntimeException exception) {
            if (videoPlayback != null) videoPlayback.cancelSaveReservation(cacheSessionId);
            serviceSaveInProgress = false;
            saveInProgress = false;
            saveView.setEnabled(true);
            saveView.setText(R.string.xhs_preview_save);
            Toast.makeText(this, "启动保存任务失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "runtime save start failed, error=" + exception.getClass().getSimpleName());
        }
    }

    private void updateRuntimeSave(XhsSaveSummary summary) {
        saveView.setText(summary.getCurrentMessage());
        if (summary.isComplete()) {
            serviceSaveInProgress = false;
            saveInProgress = false;
            saveView.setEnabled(true);
            saveView.setText(summary.hasAnySuccess() ? "已保存 · 返回清单" : "保存失败 · 返回清单重试");
            Toast.makeText(this, summary.getCurrentMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentItem() {
        if (parseResult == null || mediaItems == null || mediaItems.isEmpty()) {
            return;
        }
        final int currentPosition = previewPager == null ? 0 : previewPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= mediaItems.size()) {
            return;
        }
        final XhsMediaItem mediaItem = mediaItems.get(currentPosition);
        saveInProgress = true;
        saveView.setEnabled(false);
        saveView.setText(R.string.xhs_preview_saving);
        saveExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final XhsSaveItemResult saveItemResult = mediaSaver.save(
                            XhsPreviewActivity.this,
                            parseResult,
                            mediaItem,
                            currentPosition + 1
                    );
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSaveFinished(saveItemResult.getMessage());
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSaveFinished(getString(R.string.xhs_preview_save_failed));
                        }
                    });
                }
            }
        });
    }

    private void onSaveFinished(String message) {
        saveInProgress = false;
        saveView.setEnabled(true);
        saveView.setText(R.string.xhs_preview_save);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSaveDialog() {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return;
        }
        XhsMediaItem mediaItem = mediaItems.get(previewPager.getCurrentItem());
        int titleRes = mediaItem.getMediaType() == XhsMediaType.VIDEO
                ? R.string.xhs_preview_save_video
                : (mediaItem.getMediaType() == XhsMediaType.PDF
                ? R.string.xhs_preview_save_file
                : R.string.xhs_preview_save_image);
        new AlertDialog.Builder(this)
                .setItems(new CharSequence[]{getString(titleRes)}, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int which) {
                        triggerSaveCurrentItem();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                triggerSaveCurrentItem();
            } else {
                Toast.makeText(this, R.string.xhs_preview_save_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PreviewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mediaItems == null ? 0 : mediaItems.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View pageView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_xhs_preview_page, container, false);
            final XhsMediaItem mediaItem = mediaItems.get(position);
            java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
            headers.put("User-Agent", XhsHttpClient.MOBILE_USER_AGENT);
            if (mediaItem.getSourcePageUrl() != null) headers.put("Referer", mediaItem.getSourcePageUrl());
            if (mediaItem.requiresRuntimeSession()) {
                try {
                    headers = RuntimeMediaSessionStore.getInstance().previewHeaders(mediaItem);
                    saveView.setText(R.string.xhs_preview_save);
                } catch (IllegalStateException error) {
                    Toast.makeText(XhsPreviewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    container.addView(pageView);
                    return pageView;
                }
            }
            final ImageView imageView = pageView.findViewById(R.id.preview_image);
            final CaptainVideoPlayer videoView = pageView.findViewById(R.id.preview_video);
            final ProgressBar loadingView = pageView.findViewById(R.id.preview_loading);
            pageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (position == previewPager.getCurrentItem()) {
                        showSaveDialog();
                        return true;
                    }
                    return false;
                }
            });

            if (mediaItem.getMediaType() == XhsMediaType.VIDEO) {
                loadingView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                // 页面提供 poster 时先显示；真正首帧由播放器解码后替换。
                String poster = mediaItem.getCoverUrl();
                boolean hasPoster = poster != null
                        && (poster.startsWith("https://") || poster.startsWith("http://"))
                        && !poster.equals(mediaItem.getMediaUrl());
                // 全屏和普通视图各自绑定同一封面，避免搬移 View 或提前撤封面。
                imageView.setVisibility(View.GONE);
                GlideUrl posterSource = null;
                if (hasPoster) {
                    LazyHeaders.Builder posterHeaders = new LazyHeaders.Builder();
                    if (mediaItem.getSourcePageUrl() != null) {
                        posterHeaders.setHeader("Referer", mediaItem.getSourcePageUrl());
                    }
                    String cookie = CookieManager.getInstance().getCookie(poster);
                    if (cookie != null && !cookie.isEmpty()) posterHeaders.setHeader("Cookie", cookie);
                    posterSource = new GlideUrl(poster, posterHeaders.build());
                }
                videoView.setPreviewPoster(posterSource);
                videoPlayback.bind(position, videoView, mediaItem, headers, loadingView, () -> {
                    imageView.setVisibility(View.GONE);
                    loadingView.setVisibility(View.GONE);
                });
            } else if (mediaItem.getMediaType() == XhsMediaType.PDF) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setImageResource(R.drawable.ic_file_save);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openPdf(mediaItem.getMediaUrl());
                    }
                });
                loadingView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
            } else {
                loadingView.setVisibility(View.VISIBLE);
                LazyHeaders.Builder headerBuilder = new LazyHeaders.Builder();
                for (java.util.Map.Entry<String, String> header : headers.entrySet()) {
                    headerBuilder.setHeader(header.getKey(), header.getValue());
                }
                Glide.with(imageView.getContext())
                        .load(new GlideUrl(mediaItem.getMediaUrl(), headerBuilder.build()))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(GlideException error, Object model,
                                                        Target<Drawable> target, boolean first) {
                                loadingView.setVisibility(View.GONE);
                                Log.w(TAG, "image preview failed, host="
                                        + Uri.parse(mediaItem.getMediaUrl()).getHost()
                                        + ", error=" + (error == null ? "unknown"
                                        : error.getClass().getSimpleName()));
                                Toast.makeText(XhsPreviewActivity.this,
                                        "图片预览失败，请返回网页刷新后重试", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource source,
                                                           boolean first) {
                                loadingView.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
                videoView.setVisibility(View.GONE);
            }

            container.addView(pageView);
            return pageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (videoPlayback != null) videoPlayback.remove(position);
            container.removeView((View) object);
        }
    }

    private void openPdf(String pdfUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.xhs_preview_open_pdf_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
