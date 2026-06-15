package com.shuqiang.captain.timer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

// 时间水滴频道入口页，独立横屏沉浸运行，不影响主首页壳层。
public final class TimerChannelActivity extends Activity {
    private static final long WEATHER_REFRESH_INTERVAL_MS = 30L * 60L * 1000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TimerWeatherRepository weatherRepository = new TimerWeatherRepository();
    private TimerChannelView timerChannelView;
    private boolean destroyed;

    private final Runnable weatherRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshWeather();
            mainHandler.postDelayed(this, WEATHER_REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        allowLandscapeShortEdges();
        timerChannelView = new TimerChannelView(this);
        setContentView(timerChannelView);
        enterImmersiveMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        destroyed = false;
        enterImmersiveMode();
        timerChannelView.start();
        mainHandler.removeCallbacks(weatherRefreshRunnable);
        weatherRefreshRunnable.run();
    }

    @Override
    protected void onPause() {
        timerChannelView.stop();
        mainHandler.removeCallbacks(weatherRefreshRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void refreshWeather() {
        weatherRepository.load(this, new TimerWeatherRepository.Callback() {
            @Override
            public void onWeatherLoaded(@NonNull TimerWeatherInfo weatherInfo) {
                mainHandler.post(() -> {
                    if (!destroyed && timerChannelView != null) {
                        timerChannelView.setWeatherInfo(weatherInfo);
                    }
                });
            }

            @Override
            public void onWeatherFailed() {
                mainHandler.post(() -> {
                    if (!destroyed && timerChannelView != null) {
                        timerChannelView.setWeatherInfo(null);
                    }
                });
            }
        });
    }

    private void enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void allowLandscapeShortEdges() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(params);
    }
}
