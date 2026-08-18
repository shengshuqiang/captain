package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsRequestMode;
import com.shuqiang.captain.xhs.parser.XhsHttpClient;
import com.shuqiang.captain.xhs.parser.XhsNetworkPolicy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 进程内一次性会话仓库；Intent 只携带随机句柄，Cookie 和短效 URL 只在领取后存活到任务结束。
 */
public final class RuntimeMediaSessionStore {
    private static final long DEFAULT_TTL_MS = 15 * 60 * 1000L;
    private static final RuntimeMediaSessionStore INSTANCE = new RuntimeMediaSessionStore(DEFAULT_TTL_MS);

    public interface CookieProvider {
        String getCookie(String url);
    }

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, RuntimeMediaSession> sessions = new LinkedHashMap<>();
    private final long ttlMs;

    RuntimeMediaSessionStore(long ttlMs) {
        this.ttlMs = Math.max(1L, ttlMs);
    }

    public static RuntimeMediaSessionStore getInstance() {
        return INSTANCE;
    }

    public synchronized XhsParseResult capture(XhsParseResult rawResult, String userAgent,
                                               String finalPageUrl, CookieProvider cookieProvider) {
        if (rawResult == null || rawResult.getMediaItems() == null || rawResult.getMediaItems().isEmpty()) {
            throw new IllegalArgumentException("没有可登记的页面媒体");
        }
        cleanupExpiredLocked(System.currentTimeMillis());
        String pageUrl = XhsNetworkPolicy.isAllowedMediaUrl(finalPageUrl)
                ? finalPageUrl : rawResult.getPageUrl();
        String safeUserAgent = userAgent == null || userAgent.trim().isEmpty()
                ? XhsHttpClient.MOBILE_USER_AGENT : userAgent;
        String sessionId = randomId();
        Map<String, RuntimeMediaSession.Candidate> candidates = new LinkedHashMap<>();
        Map<String, String> cookies = new LinkedHashMap<>();
        captureCookie(cookies, pageUrl, cookieProvider);
        ArrayList<XhsMediaItem> registeredItems = new ArrayList<>();
        for (XhsMediaItem item : rawResult.getMediaItems()) {
            if (item == null || !XhsNetworkPolicy.isAllowedMediaUrl(item.getMediaUrl())) {
                continue;
            }
            String candidateId = randomId();
            candidates.put(candidateId, new RuntimeMediaSession.Candidate(
                    item.getMediaUrl(), item.getTransport(), item.getQualityHeight(), pageUrl));
            captureCookie(cookies, item.getMediaUrl(), cookieProvider);
            registeredItems.add(new XhsMediaItem(
                    item.getId(), item.getMediaType(), item.getMediaUrl(), item.getCoverUrl(),
                    item.getWidth(), item.getHeight(), item.getDurationSec(), item.getFileExtension(),
                    item.isSelected(), item.getTransport(), pageUrl, XhsRequestMode.WEBVIEW,
                    item.getSourceKey(), item.getQualityHeight(), sessionId, candidateId
            ));
        }
        if (registeredItems.isEmpty()) {
            throw new IllegalArgumentException("页面媒体地址无效");
        }
        sessions.put(sessionId, new RuntimeMediaSession(
                sessionId, safeUserAgent, pageUrl, System.currentTimeMillis() + ttlMs, candidates, cookies));
        return new XhsParseResult(
                rawResult.getNoteId(), pageUrl, pageUrl, rawResult.getAuthorName(), rawResult.getTitle(),
                rawResult.getCoverUrl(), rawResult.getParseStrategy(), rawResult.getEntrySource(), registeredItems
        );
    }

    public synchronized RuntimeMediaSession claim(String sessionId) {
        cleanupExpiredLocked(System.currentTimeMillis());
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        RuntimeMediaSession session = sessions.remove(sessionId);
        if (session != null && session.isExpired(System.currentTimeMillis())) {
            session.close();
            return null;
        }
        return session;
    }

    public synchronized void discard(String sessionId) {
        RuntimeMediaSession session = sessions.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }

    synchronized int sizeForTest() {
        cleanupExpiredLocked(System.currentTimeMillis());
        return sessions.size();
    }

    private void captureCookie(Map<String, String> cookies, String url, CookieProvider provider) {
        String origin = RuntimeMediaSession.originOf(url);
        if (origin == null || cookies.containsKey(origin) || provider == null) {
            return;
        }
        try {
            String header = provider.getCookie(url);
            if (header != null && !header.trim().isEmpty()) {
                cookies.put(origin, header);
            }
        } catch (RuntimeException ignored) {
            // 单一来源 Cookie 读取失败不应阻断其它公开媒体提取。
        }
    }

    private void cleanupExpiredLocked(long nowMs) {
        Iterator<Map.Entry<String, RuntimeMediaSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            RuntimeMediaSession session = iterator.next().getValue();
            if (session.isExpired(nowMs)) {
                iterator.remove();
                session.close();
            }
        }
    }

    private String randomId() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            builder.append(Character.forDigit(value & 0x0f, 16));
        }
        return builder.toString();
    }
}
