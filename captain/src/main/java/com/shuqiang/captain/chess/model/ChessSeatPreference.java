package com.shuqiang.captain.chess.model;

public enum ChessSeatPreference {
    WHITE("执白"),
    BLACK("执黑"),
    RANDOM("随机");

    private final String label;

    ChessSeatPreference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ChessSeatPreference fromValue(String value) {
        for (ChessSeatPreference preference : values()) {
            if (preference.name().equalsIgnoreCase(value)) {
                return preference;
            }
        }
        return RANDOM;
    }
}
