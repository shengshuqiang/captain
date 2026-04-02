package com.shuqiang.captain.chess.model;

public class ChessReplayFrame {
    private final int moveIndex;
    private final String san;
    private final String fen;
    private final ChessColor activeColor;
    private final boolean check;
    private final String capturedPiece;
    private final String promotion;

    public ChessReplayFrame(int moveIndex, String san, String fen, ChessColor activeColor,
            boolean check, String capturedPiece, String promotion) {
        this.moveIndex = moveIndex;
        this.san = san;
        this.fen = fen;
        this.activeColor = activeColor;
        this.check = check;
        this.capturedPiece = capturedPiece;
        this.promotion = promotion;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public String getSan() {
        return san;
    }

    public String getFen() {
        return fen;
    }

    public ChessColor getActiveColor() {
        return activeColor;
    }

    public boolean isCheck() {
        return check;
    }

    public String getCapturedPiece() {
        return capturedPiece;
    }

    public String getPromotion() {
        return promotion;
    }
}
