package fulguris.integration;

import android.app.Activity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

/** Host-owned resource observation; the upstream browser retains all page callbacks. */
public final class CaptainBrowserBridge {
    public interface Listener {
        void onPageStarted(WebView view, String url);
        void onRequest(WebView view, WebResourceRequest request);
        void onPageFinished(WebView view);
        void onDestroyed(WebView view);
        void openResources(Activity activity, WebView view, boolean incognito);
    }

    private static volatile Listener listener;
    private CaptainBrowserBridge() { }
    public static void setListener(Listener value) { listener = value; }
    public static void pageStarted(WebView view, String url) {
        Listener current = listener;
        if (current != null) current.onPageStarted(view, url);
    }
    public static void request(WebView view, WebResourceRequest request) {
        Listener current = listener;
        if (current != null) current.onRequest(view, request);
    }
    public static void pageFinished(WebView view) {
        Listener current = listener;
        if (current != null) current.onPageFinished(view);
    }
    public static void destroyed(WebView view) {
        Listener current = listener;
        if (current != null && view != null) current.onDestroyed(view);
    }
    public static void openResources(Activity activity, WebView view, boolean incognito) {
        Listener current = listener;
        if (current != null) current.openResources(activity, view, incognito);
    }
}
