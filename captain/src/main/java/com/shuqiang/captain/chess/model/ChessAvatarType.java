package com.shuqiang.captain.chess.model;

public enum ChessAvatarType {
    PRESET,
    LOCAL;

    public static ChessAvatarType fromValue(String value) {
        for (ChessAvatarType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return PRESET;
    }
}
