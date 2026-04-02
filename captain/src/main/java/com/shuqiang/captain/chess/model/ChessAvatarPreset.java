package com.shuqiang.captain.chess.model;

public class ChessAvatarPreset {
    private final String value;
    private final String label;
    private final int drawableRes;

    public ChessAvatarPreset(String value, String label, int drawableRes) {
        this.value = value;
        this.label = label;
        this.drawableRes = drawableRes;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public int getDrawableRes() {
        return drawableRes;
    }
}
