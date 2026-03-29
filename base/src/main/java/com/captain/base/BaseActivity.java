package com.captain.base;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化布局
        setContentView(getContentViewResource());
        // 标题栏
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        initToolbar(appBarLayout, toolbar);
        // 需要重写
        int contentLayoutResource = getContentLayoutResource();
        if (contentLayoutResource != 0) {
            // 设置内容布局
            ViewStub contentStub = (ViewStub) findViewById(R.id.content_stub);
            contentStub.setLayoutResource(contentLayoutResource);
            contentStub.inflate();
        }
    }

    protected int getContentViewResource() {
       return R.layout.activity_base;
    }

    protected void initToolbar(View appBarLayout, Toolbar toolbar) {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.drawable.bg_toolbar_surface);
        toolbar.setTitleTextColor(0xFF241C2D);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationContentDescription(R.string.toolbar_navigate_up);
        toolbar.setElevation(0f);
        if (appBarLayout instanceof AppBarLayout) {
            ((AppBarLayout) appBarLayout).setBackgroundResource(R.drawable.bg_toolbar_surface);
            ((AppBarLayout) appBarLayout).setElevation(0f);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        // 统一所有页面的标题栏返回按钮和标题样式。
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_toolbar_back);
        actionBar.setHomeButtonEnabled(true);
    }

    // 子组件重写返回内容布局
    protected int getContentLayoutResource() {
        return 0;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            // 返回键
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
