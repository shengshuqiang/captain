package com.shuqiang.captain.xhs.download.hls;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 解析 MVP 支持的 Master/Media Playlist；能力拒绝由下载引擎统一给出用户可读错误。
 */
public final class HlsPlaylistParser {
    public HlsPlaylist parse(String playlistUrl, String content) throws IOException {
        if (content == null || !content.trim().startsWith("#EXTM3U")) {
            throw new IOException("响应不是有效的 HLS 清单");
        }
        String[] lines = content.replace("\r", "").split("\n");
        ArrayList<HlsPlaylist.Variant> variants = new ArrayList<>();
        ArrayList<HlsPlaylist.Segment> segments = new ArrayList<>();
        Map<String, String> pendingVariant = null;
        HlsPlaylist.Key currentKey = null;
        double pendingDuration = 0;
        long mediaSequence = 0;
        boolean endList = false;
        boolean hasMap = false;
        String initSegmentUrl = null;
        boolean hasMultipleMaps = false;
        boolean hasDiscontinuity = false;
        boolean hasByteRange = false;
        boolean hasAlternateAudio = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#EXT-X-STREAM-INF:")) {
                pendingVariant = parseAttributes(afterColon(line));
            } else if (line.startsWith("#EXT-X-MEDIA:")) {
                Map<String, String> attributes = parseAttributes(afterColon(line));
                hasAlternateAudio |= "AUDIO".equalsIgnoreCase(attributes.get("TYPE"));
            } else if (line.startsWith("#EXT-X-MEDIA-SEQUENCE:")) {
                mediaSequence = parseLong(afterColon(line), 0);
            } else if (line.startsWith("#EXTINF:")) {
                String duration = afterColon(line);
                int comma = duration.indexOf(',');
                pendingDuration = parseDouble(comma < 0 ? duration : duration.substring(0, comma), 0);
            } else if (line.startsWith("#EXT-X-KEY:")) {
                currentKey = parseKey(playlistUrl, parseAttributes(afterColon(line)));
            } else if (line.startsWith("#EXT-X-MAP:")) {
                hasMap = true;
                Map<String, String> attributes = parseAttributes(afterColon(line));
                String mapUri = attributes.get("URI");
                if (mapUri == null || mapUri.isEmpty()) {
                    throw new IOException("HLS 初始化片段地址缺失");
                }
                String resolvedMap = resolveUrl(playlistUrl, mapUri);
                hasMultipleMaps |= initSegmentUrl != null && !initSegmentUrl.equals(resolvedMap);
                initSegmentUrl = resolvedMap;
                hasByteRange |= attributes.containsKey("BYTERANGE");
            } else if (line.startsWith("#EXT-X-BYTERANGE:")) {
                hasByteRange = true;
            } else if (line.startsWith("#EXT-X-DISCONTINUITY")) {
                hasDiscontinuity = true;
            } else if (line.startsWith("#EXT-X-ENDLIST")) {
                endList = true;
            } else if (!line.startsWith("#")) {
                String resolvedUrl = resolveUrl(playlistUrl, line);
                if (pendingVariant != null) {
                    variants.add(buildVariant(resolvedUrl, pendingVariant));
                    pendingVariant = null;
                } else {
                    segments.add(new HlsPlaylist.Segment(resolvedUrl, pendingDuration,
                            mediaSequence + segments.size(), currentKey));
                    pendingDuration = 0;
                }
            }
        }
        HlsPlaylist.Type type = variants.isEmpty() ? HlsPlaylist.Type.MEDIA : HlsPlaylist.Type.MASTER;
        return new HlsPlaylist(type, variants, segments, endList, hasMap,
                initSegmentUrl, hasMultipleMaps, hasDiscontinuity,
                hasByteRange, hasAlternateAudio);
    }

    private HlsPlaylist.Variant buildVariant(String url, Map<String, String> attributes) {
        int width = 0;
        int height = 0;
        String resolution = attributes.get("RESOLUTION");
        if (resolution != null) {
            String[] dimensions = resolution.toLowerCase(Locale.US).split("x");
            if (dimensions.length == 2) {
                width = (int) parseLong(dimensions[0], 0);
                height = (int) parseLong(dimensions[1], 0);
            }
        }
        return new HlsPlaylist.Variant(url, parseLong(attributes.get("BANDWIDTH"), 0), width, height,
                attributes.get("CODECS"), attributes.get("AUDIO"));
    }

    private HlsPlaylist.Key parseKey(String playlistUrl, Map<String, String> attributes) throws IOException {
        String method = attributes.get("METHOD");
        if (method == null || "NONE".equalsIgnoreCase(method)) {
            return null;
        }
        String uri = attributes.get("URI");
        return new HlsPlaylist.Key(method, uri == null ? null : resolveUrl(playlistUrl, uri),
                attributes.get("IV"), attributes.get("KEYFORMAT"));
    }

    static Map<String, String> parseAttributes(String rawValue) {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (rawValue == null) {
            return attributes;
        }
        List<String> parts = splitAttributes(rawValue);
        for (String part : parts) {
            int equals = part.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String name = part.substring(0, equals).trim().toUpperCase(Locale.US);
            String value = part.substring(equals + 1).trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            attributes.put(name, value);
        }
        return attributes;
    }

    private static List<String> splitAttributes(String rawValue) {
        ArrayList<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        int start = 0;
        for (int index = 0; index < rawValue.length(); index++) {
            char current = rawValue.charAt(index);
            if (current == '"') {
                inQuotes = !inQuotes;
            } else if (current == ',' && !inQuotes) {
                parts.add(rawValue.substring(start, index));
                start = index + 1;
            }
        }
        parts.add(rawValue.substring(start));
        return parts;
    }

    private static String resolveUrl(String baseUrl, String reference) throws IOException {
        try {
            URI resolved = new URI(baseUrl).resolve(reference);
            String scheme = resolved.getScheme();
            if (resolved.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IOException("HLS 子资源 URL 非法");
            }
            return resolved.toString();
        } catch (IllegalArgumentException | java.net.URISyntaxException exception) {
            throw new IOException("HLS 子资源 URL 非法", exception);
        }
    }

    private static String afterColon(String line) {
        int colon = line.indexOf(':');
        return colon < 0 ? "" : line.substring(colon + 1).trim();
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value == null ? "" : value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
