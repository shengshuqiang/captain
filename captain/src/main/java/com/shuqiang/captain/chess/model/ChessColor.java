package com.shuqiang.captain.chess.model;

import com.github.bhlangonijr.chesslib.Side;

public enum ChessColor {
    WHITE("白方"),
    BLACK("黑方");

    private final String label;

    ChessColor(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Side toSide() {
        return this == WHITE ? Side.WHITE : Side.BLACK;
    }

    public ChessColor flip() {
        return this == WHITE ? BLACK : WHITE;
    }

    public static ChessColor fromSide(Side side) {
        return side == Side.BLACK ? BLACK : WHITE;
    }
}
