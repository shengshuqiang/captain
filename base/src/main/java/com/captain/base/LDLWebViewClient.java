package com.captain.base;

import android.content.Context;
import android.os.PowerManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LDLWebViewClient extends BaseWebViewClient {
	// 离线化 url 映射关系
	static {
		OFFLINE_URL_MAP.put(R.string.ledongli_index_html, "ledongli_index.html");
		OFFLINE_URL_MAP.put(R.string.ledongli_9367_js, "ledongli_9367.js");
		OFFLINE_URL_MAP.put(R.string.ledongli_465_js, "ledongli_465.js");
		OFFLINE_URL_MAP.put(R.string.ledongli_9367_css, "ledongli_9367.css");
		OFFLINE_URL_MAP.put(R.string.ledongli_app_js, "ledongli_app.js");
	}
	// url劫持列表
	private static Set<Integer> HIJACK_URL_IDS_SET = new HashSet<>();
	static {
		HIJACK_URL_IDS_SET.add(R.string.ledongli_index_html);
		HIJACK_URL_IDS_SET.add(R.string.ledongli_9367_js);
		HIJACK_URL_IDS_SET.add(R.string.ledongli_465_js);
		HIJACK_URL_IDS_SET.add(R.string.ledongli_9367_css);
		HIJACK_URL_IDS_SET.add(R.string.ledongli_app_js);
	}

	public LDLWebViewClient(Context context) {
		super(context);
	}

	@Override
	protected boolean isInterceptedRequest(Integer resID) {
		if (HIJACK_URL_IDS_SET.contains(resID)) {
			return true;
		}
		return super.isInterceptedRequest(resID);
	}
}
