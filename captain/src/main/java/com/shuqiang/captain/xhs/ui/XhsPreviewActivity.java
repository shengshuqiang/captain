package com.shuqiang.captain.xhs.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
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
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
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
import java.util.HashMap;
import java.util.Map;
import android.webkit.CookieManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.shuqiang.captain.xhs.parser.XhsHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import captain.R;

/**
 * 统一承接图片预览和视频播放，并复用下载能力提供单项保存。
 */
public class XhsPreviewActivity extends AppCompatActivity {
    private static final String TAG = "XhsPreviewActivity";
    private static final int REQUEST_WRITE_STORAGE = 3001;
    private static final String EXTRA_PARSE_RESULT = "extra_parse_result";
    private static final String EXTRA_POSITION = "extra_position";

    private final SparseArray<ExoPlayer> videoPlayers = new SparseArray<>();
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
            if (saveInProgress && mediaItems != null && !mediaItems.isEmpty()
                    && mediaItems.get(0).requiresRuntimeSession()) {
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
        parseResult = (XhsParseResult) getIntent().getSerializableExtra(EXTRA_PARSE_RESULT);
        mediaItems = parseResult == null ? null : parseResult.getMediaItems();
        int startPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        if (mediaItems == null || mediaItems.isEmpty()) {
            finish();
            return;
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
        if (saveInProgress) {
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
        super.onDestroy();
    }

    private void updateIndicator(int position) {
        indicatorView.setText(getString(R.string.xhs_preview_indicator, position + 1, mediaItems.size()));
    }

    private void pauseAllVideos() {
        for (int i = 0; i < videoPlayers.size(); i++) videoPlayers.valueAt(i).pause();
    }

    private void stopAllVideos() {
        for (int i = 0; i < videoPlayers.size(); i++) videoPlayers.valueAt(i).release();
        videoPlayers.clear();
    }

    private void resumeCurrentVideo(int activePosition) {
        pauseAllVideos();
        ExoPlayer player = videoPlayers.get(activePosition);
        if (player != null) player.play();
    }

    /** 每次媒体请求只读取该地址所属站点的浏览器 Cookie，HLS 分片也遵守同一规则。 */
    private OkHttpDataSource.Factory previewDataSource(Map<String, String> sessionHeaders) {
        final String userAgent = sessionHeaders.containsKey("User-Agent")
                ? sessionHeaders.get("User-Agent") : XhsHttpClient.MOBILE_USER_AGENT;
        Map<String, String> defaults = new HashMap<>(sessionHeaders);
        defaults.remove("Cookie");
        defaults.put("User-Agent", userAgent);
        OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(chain -> {
            Request request = chain.request();
            String cookie = CookieManager.getInstance().getCookie(request.url().toString());
            Request.Builder builder = request.newBuilder().removeHeader("Cookie");
            if (cookie != null && !cookie.trim().isEmpty()) builder.header("Cookie", cookie);
            return chain.proceed(builder.build());
        }).build();
        return new OkHttpDataSource.Factory(client).setDefaultRequestProperties(defaults);
    }

    private void triggerSaveCurrentItem() {
        if (saveInProgress || mediaItems == null || mediaItems.isEmpty()) {
            return;
        }
        if (mediaItems.get(previewPager.getCurrentItem()).requiresRuntimeSession()) {
            if (runtimeSessionHandedOff) {
                finish();
            } else {
                startRuntimeSave();
            }
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
        saveCurrentItem();
    }

    /** 预览页单项保存沿用前台服务与一次性会话，离开页面也能完成。 */
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
        saveView.setEnabled(false);
        saveView.setText("正在保存所选资源…");
        try {
            Intent intent = XhsDownloadContract.buildStartIntent(this, parseResult);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            runtimeSessionHandedOff = true;
        } catch (RuntimeException exception) {
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
                saveCurrentItem();
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
            java.util.Map<String, String> headers = java.util.Collections.emptyMap();
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
            final PlayerView videoView = pageView.findViewById(R.id.preview_video);
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
                // 视频首帧由播放器提供，避免为了封面再下载另一条媒体。
                imageView.setVisibility(View.GONE);
                OkHttpDataSource.Factory httpFactory = previewDataSource(headers);
                DefaultDataSource.Factory sourceFactory = new DefaultDataSource.Factory(
                        XhsPreviewActivity.this, httpFactory);
                com.google.android.exoplayer2.MediaItem source =
                        com.google.android.exoplayer2.MediaItem.fromUri(mediaItem.getMediaUrl());
                ExoPlayer player = new ExoPlayer.Builder(XhsPreviewActivity.this).build();
                videoView.setPlayer(player);
                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) loadingView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPlayerError(PlaybackException error) {
                        loadingView.setVisibility(View.GONE);
                        Log.w(TAG, "video preview failed, transport=" + mediaItem.getTransport()
                                + ", host=" + Uri.parse(mediaItem.getMediaUrl()).getHost()
                                + ", code=" + error.getErrorCodeName()
                                + ", cause=" + (error.getCause() == null ? "unknown"
                                : error.getCause().getClass().getSimpleName()));
                        Toast.makeText(XhsPreviewActivity.this,
                                "播放失败：" + error.getErrorCodeName() + "，可返回网页重试",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                if (mediaItem.getTransport() == XhsMediaTransport.HLS_STREAM) {
                    player.setMediaSource(new HlsMediaSource.Factory(sourceFactory).createMediaSource(source));
                } else {
                    player.setMediaSource(new ProgressiveMediaSource.Factory(sourceFactory)
                            .createMediaSource(source));
                }
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
                videoPlayers.put(position, player);
                player.prepare();
                if (position == previewPager.getCurrentItem()) player.play();
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
                LazyHeaders.Builder headerBuilder = new LazyHeaders.Builder();
                for (java.util.Map.Entry<String, String> header : headers.entrySet()) {
                    headerBuilder.setHeader(header.getKey(), header.getValue());
                }
                Glide.with(imageView.getContext())
                        .load(new GlideUrl(mediaItem.getMediaUrl(), headerBuilder.build()))
                        .into(imageView);
                loadingView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
            }

            container.addView(pageView);
            return pageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ExoPlayer player = videoPlayers.get(position);
            if (player != null) {
                player.release();
                videoPlayers.remove(position);
            }
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
