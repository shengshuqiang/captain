package com.luck.picture.lib.basic;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.captain.base.BaseActivity;
import com.luck.picture.lib.PictureSelectorFragment;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.immersive.ImmersiveManager;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.language.PictureLanguageUtils;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.utils.StyleUtils;

/**
 * @author：luck
 * @date：2021/11/17 9:59 上午
 * @describe：PictureSelectorSupporterActivity
 */
public class PictureSelectorSupporterActivity extends BaseActivity {
    private SelectorConfig selectorConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initSelectorConfig();
        immersive();
        super.onCreate(savedInstanceState);
        setupFragment();
    }

    @Override
    protected void initToolbar(View appBarLayout, Toolbar toolbar) {
        // 自定义了标题栏，屏蔽父组件操作
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.ps_activity_container;
    }

    private void initSelectorConfig() {
        selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
    }

    private void immersive() {
        SelectMainStyle mainStyle = selectorConfig.selectorStyle.getSelectMainStyle();
        int statusBarColor = mainStyle.getStatusBarColor();
        int navigationBarColor = mainStyle.getNavigationBarColor();
        boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        }
        if (!StyleUtils.checkStyleValidity(navigationBarColor)) {
            navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary);
        }
        ImmersiveManager.immersiveAboveAPI23(this, statusBarColor, navigationBarColor, isDarkStatusBarBlack);
    }

    private void setupFragment() {
        PictureSelectorFragment pictureSelectorFragment = PictureSelectorFragment.newInstance();
        FragmentInjectManager.injectFragment(this, PictureSelectorFragment.TAG, pictureSelectorFragment);
    }

    /**
     * set app language
     */
    public void initAppLanguage() {
        if (selectorConfig != null && selectorConfig.language != LanguageConfig.UNKNOWN_LANGUAGE && !selectorConfig.isOnlyCamera) {
            PictureLanguageUtils.setAppLanguage(this, selectorConfig.language, selectorConfig.defaultLanguage);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initAppLanguage();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig != null) {
            super.attachBaseContext(PictureContextWrapper.wrap(newBase, selectorConfig.language, selectorConfig.defaultLanguage));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (selectorConfig != null) {
            PictureWindowAnimationStyle windowAnimationStyle = selectorConfig.selectorStyle.getWindowAnimationStyle();
            overridePendingTransition(0, windowAnimationStyle.activityExitAnimation);
        }
    }
}
