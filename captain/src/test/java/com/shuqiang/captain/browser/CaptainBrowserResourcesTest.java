package com.shuqiang.captain.browser;

import android.app.Activity;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import static org.junit.Assert.*;

/** Returning to the browser's local homepage must detach the previous web document. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class CaptainBrowserResourcesTest {
    @Test public void localHomepageCannotExportThePreviousWebPagesVideo() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        WebView view = new WebView(activity);
        CaptainBrowserResources resources = new CaptainBrowserResources();
        try {
            resources.onPageStarted(view, "https://example.com/watch");
            resources.onRequest(view, new WebResourceRequest() {
                @Override public Uri getUrl() { return Uri.parse("https://example.com/video.mp4"); }
                @Override public boolean isForMainFrame() { return false; }
                @Override public boolean isRedirect() { return false; }
                @Override public boolean hasGesture() { return false; }
                @Override public String getMethod() { return "GET"; }
                @Override public Map<String, String> getRequestHeaders() { return Collections.emptyMap(); }
            });
            view.loadUrl("file:///browser/homepage.html");
            resources.onPageStarted(view, "file:///browser/homepage.html");
            resources.openResources(activity, view, false);
            assertEquals("当前页面没有可保存的网页资源", ShadowToast.getTextOfLatestToast());
        } finally {
            resources.onDestroyed(view);
            view.destroy();
            activity.finish();
        }
    }
}
