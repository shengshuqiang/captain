package com.shuqiang.captain.xhs.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * 页面中提取到的单个视频或图片资源。
 */
public class XhsMediaItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final XhsMediaType mediaType;
    private final String mediaUrl;
    private final String coverUrl;
    private final int width;
    private final int height;
    private final int durationSec;
    private final String fileExtension;
    private final XhsMediaTransport transport;
    private final String sourcePageUrl;
    private final XhsRequestMode requestMode;
    private final String sourceKey;
    private final int qualityHeight;
    private final String runtimeSessionId;
    private final String runtimeCandidateId;
    private boolean selected;

    public XhsMediaItem(String id, XhsMediaType mediaType, String mediaUrl, String coverUrl,
                        int width, int height, int durationSec, String fileExtension, boolean selected) {
        this(id, mediaType, mediaUrl, coverUrl, width, height, durationSec, fileExtension, selected,
                XhsMediaTransport.DIRECT_FILE, null, XhsRequestMode.AUTO, null, 0, null, null);
    }

    public XhsMediaItem(String id, XhsMediaType mediaType, String mediaUrl, String coverUrl,
                        int width, int height, int durationSec, String fileExtension, boolean selected,
                        XhsMediaTransport transport, String sourcePageUrl, XhsRequestMode requestMode,
                        String sourceKey, int qualityHeight) {
        this(id, mediaType, mediaUrl, coverUrl, width, height, durationSec, fileExtension, selected,
                transport, sourcePageUrl, requestMode, sourceKey, qualityHeight, null, null);
    }

    public XhsMediaItem(String id, XhsMediaType mediaType, String mediaUrl, String coverUrl,
                        int width, int height, int durationSec, String fileExtension, boolean selected,
                        XhsMediaTransport transport, String sourcePageUrl, XhsRequestMode requestMode,
                        String sourceKey, int qualityHeight, String runtimeSessionId,
                        String runtimeCandidateId) {
        this.id = id;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.coverUrl = coverUrl;
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.fileExtension = fileExtension;
        this.transport = transport == null ? XhsMediaTransport.DIRECT_FILE : transport;
        this.sourcePageUrl = sourcePageUrl;
        this.requestMode = requestMode == null ? XhsRequestMode.AUTO : requestMode;
        this.sourceKey = sourceKey;
        this.qualityHeight = qualityHeight;
        this.runtimeSessionId = runtimeSessionId;
        this.runtimeCandidateId = runtimeCandidateId;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public XhsMediaType getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public XhsMediaTransport getTransport() {
        return transport;
    }

    public String getSourcePageUrl() {
        return sourcePageUrl;
    }

    public XhsRequestMode getRequestMode() {
        return requestMode;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public int getQualityHeight() {
        return qualityHeight;
    }

    public String getRuntimeSessionId() {
        return runtimeSessionId;
    }

    public String getRuntimeCandidateId() {
        return runtimeCandidateId;
    }

    public boolean requiresRuntimeSession() {
        return runtimeSessionId != null && !runtimeSessionId.trim().isEmpty()
                && runtimeCandidateId != null && !runtimeCandidateId.trim().isEmpty();
    }

    public boolean requiresSourceResolution() {
        return requiresRuntimeSession() || sourceKey != null && !sourceKey.trim().isEmpty();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDisplayDuration() {
        if (durationSec <= 0) {
            return "";
        }
        int minutes = durationSec / 60;
        int seconds = durationSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
