package com.shuqiang.captain.qr.scan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class QrScanResultClassifier {
    public enum ResultType {
        EMPTY,
        WEB_URL,
        TEXT
    }

    private QrScanResultClassifier() {
    }

    @NonNull
    public static ResultInfo classify(@Nullable String rawText) {
        String displayText = rawText == null ? "" : rawText;
        String normalizedText = displayText.trim();
        if (normalizedText.length() == 0) {
            return new ResultInfo(ResultType.EMPTY, displayText, normalizedText);
        }
        if (isWebUrl(normalizedText)) {
            return new ResultInfo(ResultType.WEB_URL, displayText, normalizedText);
        }
        return new ResultInfo(ResultType.TEXT, displayText, normalizedText);
    }

    private static boolean isWebUrl(@NonNull String text) {
        try {
            URI uri = new URI(text);
            String scheme = uri.getScheme();
            String authority = uri.getRawAuthority();
            if (scheme == null || authority == null || authority.length() == 0) {
                return false;
            }
            String lowerScheme = scheme.toLowerCase(Locale.US);
            return "http".equals(lowerScheme) || "https".equals(lowerScheme);
        } catch (URISyntaxException ignored) {
            return false;
        }
    }

    public static final class ResultInfo {
        private final ResultType type;
        private final String displayText;
        private final String normalizedText;

        private ResultInfo(@NonNull ResultType type,
                           @NonNull String displayText,
                           @NonNull String normalizedText) {
            this.type = type;
            this.displayText = displayText;
            this.normalizedText = normalizedText;
        }

        @NonNull
        public ResultType getType() {
            return type;
        }

        @NonNull
        public String getDisplayText() {
            return displayText;
        }

        @NonNull
        public String getNormalizedText() {
            return normalizedText;
        }

        public boolean canOpenUrl() {
            return type == ResultType.WEB_URL;
        }

        public boolean canCopy() {
            return type != ResultType.EMPTY;
        }

        public boolean canGenerateQrCode() {
            return type != ResultType.EMPTY;
        }
    }
}
