package com.captain.base;

import android.content.Context;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BaseWebViewClient extends WebViewClient {
	public static final String URL_KEY = "url";
	// 离线化 url 映射关系
	protected static Map<Integer, String> OFFLINE_URL_MAP = new HashMap<>();
	static {
		// 隐私政策 url
		OFFLINE_URL_MAP.put(R.string.captain_privacy_policy_url, "captain_privacy_policy.html");
		OFFLINE_URL_MAP.put(R.string.captain_privacy, "captain_privacy.png");
		OFFLINE_URL_MAP.put(R.string.shengshuqiang_weixin, "shengshuqiang-weixin.jpg");
		OFFLINE_URL_MAP.put(R.string.captain_v, "captain.v.1.1.100.png");
	}

	private Context context;

	public BaseWebViewClient(Context context) {
		this.context = context;
	}

	/**
	 * 是否使用离线化，断网使用
	 * @return
	 */
	private boolean isUseOffline () {
		return !Utils.isNetworkConnected(context);
	}

	protected boolean isInterceptedRequest(Integer resID) {
		return isUseOffline();
	}

	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                long interceptRequestTime = System.currentTimeMillis();
		final String url = request.getUrl().toString();
//                Log.d("MCPN", "TestWebViewActivity#shouldInterceptRequest " + Thread.currentThread().getName() + ", interceptRequestTime=" + interceptRequestTime + ", url=" + url);
//                离线化处理
		for (Map.Entry<Integer, String> entry : OFFLINE_URL_MAP.entrySet()) {
			Integer resID = entry.getKey();
			Boolean isIntercepted = isInterceptedRequest(resID);
			System.out.println("url: " + url + ", resID: " + resID + ", isIntercepted=" + isIntercepted);
			// 无网络或者命中劫持都拦截
			if (isIntercepted) {
				// 待匹配线上 url
				String matchOnLineUrl = context.getString(resID);
				// 离线资源名称
				String offlineAssetsFileName = entry.getValue();
				System.out.println("Key: " + resID + ", Value: " + matchOnLineUrl);
				if (url.contains(matchOnLineUrl)) {
					Map<String, String> headers = new HashMap(8);
//                    headers.put("X-Intercepted-By", "maicai/debugging");
					headers.put("Connection", "keep-alive");
					String mimeType = "text/html";
					if (offlineAssetsFileName.endsWith(".html")) {
						headers.put("Content-Type", "text/html; charset=utf-8");
						mimeType = "text/html";
					} else if (offlineAssetsFileName.endsWith(".png") || offlineAssetsFileName.endsWith(".jpg") || offlineAssetsFileName.endsWith(".webp")) {
						mimeType = "image/png";
						headers.put("Content-Type", mimeType);
					} else if (offlineAssetsFileName.endsWith(".css")) {
						mimeType = "text/css";
						headers.put("Content-Type", mimeType);
					} else if (offlineAssetsFileName.endsWith(".js")) {
						mimeType = "text/javascript";
						headers.put("Content-Type", mimeType);
					}
					headers.put("Access-Control-Allow-Origin", "*");
					try {
						InputStream is = context.getAssets().open(offlineAssetsFileName);
						WebResourceResponse var6 = new WebResourceResponse(mimeType, "utf-8", 200, "OK", headers, is);
						return var6;
//                                // 有必要的话返回空取消请求
//                                return null;
					} catch (IOException var10) {
//                                仅打印日志走默认请求
						Log.i("TitansOffline", "Assets 中未找到 " + offlineAssetsFileName);
					}
				}
			}
		}

//                拦截处理
//                if() {
//
//                }
		return super.shouldInterceptRequest(view, request);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
//                Toast.makeText(WebViewActivity.this, "onPageFinished", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
//                避免ssl证书失败空白页
		handler.proceed();
	}
}
