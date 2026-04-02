package com.shuqiang.captain.chess.model;

public enum ChessConnectionState {
    IDLE,
    DISCOVERING,
    HOSTING,
    CONNECTING,
    CONNECTED,
    READY,
    PLAYING,
    FINISHED,
    DISCONNECTED
}
