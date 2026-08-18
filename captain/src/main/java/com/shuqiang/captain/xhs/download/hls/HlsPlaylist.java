package com.shuqiang.captain.xhs.download.hls;

import java.util.Collections;
import java.util.List;

/**
 * HLS 清单的最小不可变模型，只保留下载和能力判断真正需要的字段。
 */
public final class HlsPlaylist {
    public enum Type {
        MASTER,
        MEDIA
    }

    private final Type type;
    private final List<Variant> variants;
    private final List<Segment> segments;
    private final boolean endList;
    private final boolean hasMap;
    private final boolean hasDiscontinuity;
    private final boolean hasByteRange;
    private final boolean hasAlternateAudio;

    HlsPlaylist(Type type, List<Variant> variants, List<Segment> segments, boolean endList,
                boolean hasMap, boolean hasDiscontinuity, boolean hasByteRange,
                boolean hasAlternateAudio) {
        this.type = type;
        this.variants = Collections.unmodifiableList(variants);
        this.segments = Collections.unmodifiableList(segments);
        this.endList = endList;
        this.hasMap = hasMap;
        this.hasDiscontinuity = hasDiscontinuity;
        this.hasByteRange = hasByteRange;
        this.hasAlternateAudio = hasAlternateAudio;
    }

    public Type getType() {
        return type;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public boolean hasEndList() {
        return endList;
    }

    public boolean hasMap() {
        return hasMap;
    }

    public boolean hasDiscontinuity() {
        return hasDiscontinuity;
    }

    public boolean hasByteRange() {
        return hasByteRange;
    }

    public boolean hasAlternateAudio() {
        return hasAlternateAudio;
    }

    public double getTotalDurationSec() {
        double duration = 0;
        for (Segment segment : segments) {
            duration += segment.durationSec;
        }
        return duration;
    }

    public static final class Variant {
        private final String url;
        private final long bandwidth;
        private final int width;
        private final int height;
        private final String codecs;
        private final String audioGroup;

        Variant(String url, long bandwidth, int width, int height, String codecs, String audioGroup) {
            this.url = url;
            this.bandwidth = bandwidth;
            this.width = width;
            this.height = height;
            this.codecs = codecs;
            this.audioGroup = audioGroup;
        }

        public String getUrl() {
            return url;
        }

        public long getBandwidth() {
            return bandwidth;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getCodecs() {
            return codecs;
        }

        public String getAudioGroup() {
            return audioGroup;
        }
    }

    public static final class Segment {
        private final String url;
        private final double durationSec;
        private final long sequence;
        private final Key key;

        Segment(String url, double durationSec, long sequence, Key key) {
            this.url = url;
            this.durationSec = durationSec;
            this.sequence = sequence;
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public double getDurationSec() {
            return durationSec;
        }

        public long getSequence() {
            return sequence;
        }

        public Key getKey() {
            return key;
        }
    }

    public static final class Key {
        private final String method;
        private final String url;
        private final String ivHex;
        private final String keyFormat;

        Key(String method, String url, String ivHex, String keyFormat) {
            this.method = method;
            this.url = url;
            this.ivHex = ivHex;
            this.keyFormat = keyFormat;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public String getIvHex() {
            return ivHex;
        }

        public String getKeyFormat() {
            return keyFormat;
        }
    }
}
