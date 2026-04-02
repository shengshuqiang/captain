package com.shuqiang.captain.chess.model;

import java.io.Serializable;

public class ChessUserProfile implements Serializable {
    private String username;
    private ChessAvatarType avatarType;
    private String avatarValue;

    public ChessUserProfile() {
        this("", ChessAvatarType.PRESET, "");
    }

    public ChessUserProfile(String username, ChessAvatarType avatarType, String avatarValue) {
        this.username = username;
        this.avatarType = avatarType;
        this.avatarValue = avatarValue;
    }

    public ChessUserProfile copy() {
        return new ChessUserProfile(username, avatarType, avatarValue);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ChessAvatarType getAvatarType() {
        return avatarType;
    }

    public void setAvatarType(ChessAvatarType avatarType) {
        this.avatarType = avatarType;
    }

    public String getAvatarValue() {
        return avatarValue;
    }

    public void setAvatarValue(String avatarValue) {
        this.avatarValue = avatarValue;
    }
}
