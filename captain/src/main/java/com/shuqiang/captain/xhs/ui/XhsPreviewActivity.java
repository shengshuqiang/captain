package com.shuqiang.captain.xhs.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.storage.XhsMediaSaver;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import captain.R;

/**
 * 统一承接图片预览和视频播放，并复用下载能力提供单项保存。
 */
public class XhsPreviewActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 3001;
    private static final String EXTRA_PARSE_RESULT = "extra_parse_result";
    private static final String EXTRA_POSITION = "extra_position";

    private final SparseArray<VideoView> videoViews = new SparseArray<>();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final XhsMediaSaver mediaSaver = new XhsMediaSaver();

    private XhsParseResult parseResult;
    private ArrayList<XhsMediaItem> mediaItems;
    private ViewPager previewPager;
    private TextView indicatorView;
    private TextView saveView;
    private boolean saveInProgress;

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
    protected void onDestroy() {
        stopAllVideos();
        saveExecutor.shutdownNow();
        super.onDestroy();
    }

    private void updateIndicator(int position) {
        indicatorView.setText(getString(R.string.xhs_preview_indicator, position + 1, mediaItems.size()));
    }

    private void pauseAllVideos() {
        for (int i = 0; i < videoViews.size(); i++) {
            VideoView videoView = videoViews.valueAt(i);
            if (videoView != null && videoView.isPlaying()) {
                videoView.pause();
            }
        }
    }

    private void stopAllVideos() {
        for (int i = 0; i < videoViews.size(); i++) {
            VideoView videoView = videoViews.valueAt(i);
            if (videoView != null) {
                videoView.stopPlayback();
            }
        }
        videoViews.clear();
    }

    private void resumeCurrentVideo(int activePosition) {
        pauseAllVideos();
        VideoView videoView = videoViews.get(activePosition);
        if (videoView != null) {
            videoView.start();
        }
    }

    private void triggerSaveCurrentItem() {
        if (saveInProgress || mediaItems == null || mediaItems.isEmpty()) {
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
            final ImageView imageView = pageView.findViewById(R.id.preview_image);
            final VideoView videoView = pageView.findViewById(R.id.preview_video);
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
                String coverUrl = mediaItem.getCoverUrl() == null || mediaItem.getCoverUrl().isEmpty()
                        ? mediaItem.getMediaUrl()
                        : mediaItem.getCoverUrl();
                Glide.with(imageView.getContext())
                        .load(coverUrl)
                        .into(imageView);
                MediaController mediaController = new MediaController(XhsPreviewActivity.this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(Uri.parse(mediaItem.getMediaUrl()));
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        loadingView.setVisibility(View.GONE);
                        imageView.setVisibility(View.GONE);
                        mediaPlayer.setLooping(true);
                        if (position == previewPager.getCurrentItem()) {
                            videoView.start();
                        }
                    }
                });
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                        loadingView.setVisibility(View.GONE);
                        Toast.makeText(XhsPreviewActivity.this, "视频播放失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                videoViews.put(position, videoView);
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
                Glide.with(imageView.getContext())
                        .load(mediaItem.getMediaUrl())
                        .into(imageView);
                loadingView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
            }

            container.addView(pageView);
            return pageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            VideoView videoView = videoViews.get(position);
            if (videoView != null) {
                videoView.stopPlayback();
                videoViews.remove(position);
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
