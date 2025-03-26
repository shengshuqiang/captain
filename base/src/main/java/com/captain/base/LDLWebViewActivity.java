package com.captain.base;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LDLWebViewActivity extends WebViewActivity {
	private PowerManager.WakeLock wakeLock = null;
	private LDLWebAppInterface webAppInterface = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取PowerManager服务
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// 创建WakeLock
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"com.shuqiang.captain:WebViewActivity");
	}

	@Override
	protected void initWebView(WebView webView) {
		super.initWebView(webView);
		webAppInterface = new LDLWebAppInterface(this);
		webView.addJavascriptInterface(webAppInterface, "SSU");
	}

	@Override
	protected BaseWebViewClient buildWebViewClient() {
		return new LDLWebViewClient(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (wakeLock != null) {
			wakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		if (wakeLock != null) {
			wakeLock.release();
		}
		super.onPause();
	}

	@Override
	public void onReadWriteFile() {
		super.onReadWriteFile();
		webAppInterface.onReadWriteFile();
	}
}
