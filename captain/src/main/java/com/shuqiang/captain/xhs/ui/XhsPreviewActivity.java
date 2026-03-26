package com.shuqiang.captain.xhs.ui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.util.ArrayList;

import captain.R;

/**
 * 统一承接图片预览和视频播放，避免跳到外部应用打断当前下载流程。
 */
public class XhsPreviewActivity extends AppCompatActivity {
    private static final String EXTRA_MEDIA_LIST = "extra_media_list";
    private static final String EXTRA_POSITION = "extra_position";

    private final SparseArray<VideoView> videoViews = new SparseArray<>();
    private ArrayList<XhsMediaItem> mediaItems;
    private ViewPager previewPager;
    private TextView indicatorView;

    public static Intent buildIntent(Context context, ArrayList<XhsMediaItem> mediaItems, int position) {
        Intent intent = new Intent(context, XhsPreviewActivity.class);
        intent.putExtra(EXTRA_MEDIA_LIST, mediaItems);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xhs_preview);
        mediaItems = (ArrayList<XhsMediaItem>) getIntent().getSerializableExtra(EXTRA_MEDIA_LIST);
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
        findViewById(R.id.preview_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
}
