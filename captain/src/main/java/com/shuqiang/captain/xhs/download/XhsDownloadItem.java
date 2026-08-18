package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsRequestMode;

import java.io.Serializable;

/**
 * Service 使用的脱敏媒体描述；可重新解析的资源不会携带短效真实地址。
 */
public final class XhsDownloadItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final XhsMediaType mediaType;
    private final XhsMediaTransport transport;
    private final String directUrl;
    private final String sourcePageUrl;
    private final XhsRequestMode requestMode;
    private final String sourceKey;
    private final int qualityHeight;
    private final String fileExtension;
    private final String runtimeCandidateId;

    private XhsDownloadItem(String id, XhsMediaType mediaType, XhsMediaTransport transport,
                            String directUrl, String sourcePageUrl, XhsRequestMode requestMode,
                            String sourceKey, int qualityHeight, String fileExtension,
                            String runtimeCandidateId) {
        this.id = id;
        this.mediaType = mediaType;
        this.transport = transport;
        this.directUrl = directUrl;
        this.sourcePageUrl = sourcePageUrl;
        this.requestMode = requestMode;
        this.sourceKey = sourceKey;
        this.qualityHeight = qualityHeight;
        this.fileExtension = fileExtension;
        this.runtimeCandidateId = runtimeCandidateId;
    }

    static XhsDownloadItem from(XhsMediaItem mediaItem) {
        boolean runtimeResolved = mediaItem.requiresRuntimeSession();
        String safeDirectUrl = mediaItem.requiresSourceResolution() ? null : mediaItem.getMediaUrl();
        String safeSourcePageUrl = runtimeResolved ? null : mediaItem.getSourcePageUrl();
        return new XhsDownloadItem(mediaItem.getId(), mediaItem.getMediaType(), mediaItem.getTransport(),
                safeDirectUrl, safeSourcePageUrl, mediaItem.getRequestMode(), mediaItem.getSourceKey(),
                mediaItem.getQualityHeight(), mediaItem.getFileExtension(), mediaItem.getRuntimeCandidateId());
    }

    public String getId() {
        return id;
    }

    public XhsMediaType getMediaType() {
        return mediaType;
    }

    public XhsMediaTransport getTransport() {
        return transport;
    }

    public String getDirectUrl() {
        return directUrl;
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

    public String getFileExtension() {
        return fileExtension;
    }

    public String getRuntimeCandidateId() {
        return runtimeCandidateId;
    }

    public boolean requiresRuntimeSession() {
        return runtimeCandidateId != null && !runtimeCandidateId.trim().isEmpty();
    }

    /** 为显式能力回退生成实际画质副本，避免低清文件占用高画质文件名。 */
    public XhsDownloadItem withResolvedTransport(XhsMediaTransport resolvedTransport, int resolvedHeight) {
        return new XhsDownloadItem(id, mediaType, resolvedTransport, directUrl, sourcePageUrl,
                requestMode, sourceKey, resolvedHeight, fileExtension, runtimeCandidateId);
    }
}
