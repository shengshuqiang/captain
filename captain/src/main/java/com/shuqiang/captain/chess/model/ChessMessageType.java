package com.shuqiang.captain.chess.model;

public enum ChessMessageType {
    HELLO,
    READY,
    START_GAME,
    MOVE_REQUEST,
    MOVE_COMMIT,
    DRAW_OFFER,
    DRAW_RESPONSE,
    RESIGN,
    SYNC_STATE,
    HEARTBEAT,
    END_GAME
}
