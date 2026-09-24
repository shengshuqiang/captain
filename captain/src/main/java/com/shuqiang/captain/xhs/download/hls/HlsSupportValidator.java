package com.shuqiang.captain.xhs.download.hls;

import java.io.IOException;
import java.util.Locale;

/** 将 v1 支持边界集中成可测试门禁，防止不支持的清单进入分片下载。 */
public final class HlsSupportValidator {
    public HlsPlaylist.Variant selectVariant(HlsPlaylist playlist, int expectedHeight) throws IOException {
        if (playlist.getType() != HlsPlaylist.Type.MASTER || playlist.getVariants().isEmpty()) {
            throw new IOException("HLS Master Playlist 没有可用画质");
        }
        if (playlist.hasAlternateAudio()) {
            throw new IOException("暂不支持 HLS 独立音轨");
        }
        HlsPlaylist.Variant selected = null;
        if (expectedHeight > 0) {
            for (HlsPlaylist.Variant variant : playlist.getVariants()) {
                if (variant.getHeight() == expectedHeight) {
                    selected = variant;
                    break;
                }
            }
            if (selected == null && playlist.getVariants().size() == 1
                    && playlist.getVariants().get(0).getHeight() == 0) {
                selected = playlist.getVariants().get(0);
            }
            if (selected == null) {
                throw new IOException("HLS 所选画质已不存在，不自动降级");
            }
        } else {
            for (HlsPlaylist.Variant variant : playlist.getVariants()) {
                if (selected == null || variant.getHeight() > selected.getHeight()
                        || (variant.getHeight() == selected.getHeight()
                        && variant.getBandwidth() > selected.getBandwidth())) {
                    selected = variant;
                }
            }
        }
        validateCodecs(selected == null ? null : selected.getCodecs());
        return selected;
    }

    public void validateMediaPlaylist(HlsPlaylist playlist) throws IOException {
        if (playlist.getType() != HlsPlaylist.Type.MEDIA || playlist.getSegments().isEmpty()) {
            throw new IOException("HLS Media Playlist 没有视频分片");
        }
        if (!playlist.hasEndList()) {
            throw new IOException("暂不支持直播或未结束的 HLS");
        }
        if (playlist.hasMultipleMaps()) {
            throw new IOException("暂不支持切换初始化片段的 HLS");
        }
        if (playlist.hasByteRange()) {
            throw new IOException("暂不支持 HLS Byte Range 分片");
        }
        if (playlist.hasDiscontinuity()) {
            throw new IOException("暂不支持 HLS 跳轨清单");
        }
        for (HlsPlaylist.Segment segment : playlist.getSegments()) {
            HlsPlaylist.Key key = segment.getKey();
            if (key == null) {
                continue;
            }
            if (playlist.hasMap()) {
                throw new IOException("暂不支持加密的 fMP4 HLS");
            }
            if (!"AES-128".equalsIgnoreCase(key.getMethod())) {
                throw new IOException("暂不支持 HLS 加密方式 " + key.getMethod());
            }
            if (key.getUrl() == null || (key.getKeyFormat() != null
                    && !"identity".equalsIgnoreCase(key.getKeyFormat()))) {
                throw new IOException("暂不支持 DRM 或非 identity HLS 密钥");
            }
        }
    }

    private void validateCodecs(String codecs) throws IOException {
        if (codecs == null || codecs.trim().isEmpty()) {
            return;
        }
        String lower = codecs.toLowerCase(Locale.US);
        String[] tracks = lower.split(",");
        boolean hasVideo = false;
        for (String track : tracks) {
            String codec = track.trim();
            if (codec.startsWith("avc1") || codec.startsWith("avc3")
                    || codec.startsWith("hvc1") || codec.startsWith("hev1")) {
                hasVideo = true;
            } else if (!codec.startsWith("mp4a")) {
                throw new IOException("暂不支持该 HLS 编码组合");
            }
        }
        if (!hasVideo) {
            throw new IOException("HLS 清单缺少可转封装的视频编码");
        }
    }
}
