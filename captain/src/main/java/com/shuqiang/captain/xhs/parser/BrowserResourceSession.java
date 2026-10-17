package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.HashSet;
import java.util.Set;

/** One browser document's candidates; playback completion never clears discovered media. */
public final class BrowserResourceSession {
    private final WebResourceMediaCollector collector;

    public BrowserResourceSession(String pageUrl) {
        collector = new WebResourceMediaCollector(pageUrl, "fulguris_browser");
    }

    public void observeRequest(String url) { collector.observeRequest(url); }
    public String snapshotScript() { return WebResourceDomSnapshot.SCRIPT; }

    /** DOM snapshots add metadata; disappeared or ended video elements remain downloadable. */
    public void observeSnapshot(String value) throws Exception {
        if (value == null || "null".equals(value)) return;
        Object decoded = new JSONTokener(value).nextValue();
        JSONObject payload = new JSONObject(decoded instanceof String ? (String) decoded : value);
        collector.setPageTitle(payload.optString("title"));
        Set<String> playing = new HashSet<>();
        JSONArray urls = payload.optJSONArray("playingUrls");
        if (urls != null) for (int i = 0; i < urls.length(); i++) playing.add(urls.optString(i));
        collector.setCurrentPlaybackUrls(playing);
        JSONArray items = payload.optJSONArray("items");
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            collector.observeDom(item.optString("url"), item.optString("kind"),
                    item.optString("mime"), item.optInt("width"), item.optInt("height"),
                    item.optInt("sourceWidth"), item.optInt("sourceHeight"),
                    item.optBoolean("visible"), item.optBoolean("ready"), true,
                    item.optString("hints"), item.optString("poster"));
        }
    }

    public XhsParseResult snapshot() { return collector.buildResult(); }
}
