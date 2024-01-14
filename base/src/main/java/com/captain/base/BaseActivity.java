package com.captain.base;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // 标题栏
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        initToolbar(appBarLayout, toolbar);

        // 设置内容布局
        ViewStub contentStub = (ViewStub) findViewById(R.id.content_stub);
        contentStub.setLayoutResource(getContentLayoutResource());
        contentStub.inflate();
    }

    protected void initToolbar(View appBarLayout, Toolbar toolbar) {
        setSupportActionBar(toolbar);
        // 添加默认的返回图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        // 设置返回键可用
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    // 子组件重写返回内容布局
    protected abstract int getContentLayoutResource();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            // 返回键
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}