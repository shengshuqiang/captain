package com.shuqiang.captain.chess.model;

public class ChessMoveRecord {
    private final int moveIndex;
    private final String uci;
    private final String san;
    private final String fenAfterMove;
    private final ChessColor activeColor;
    private final boolean check;
    private final String capturedPiece;
    private final String promotion;

    public ChessMoveRecord(int moveIndex, String uci, String san, String fenAfterMove, ChessColor activeColor,
            boolean check, String capturedPiece, String promotion) {
        this.moveIndex = moveIndex;
        this.uci = uci;
        this.san = san;
        this.fenAfterMove = fenAfterMove;
        this.activeColor = activeColor;
        this.check = check;
        this.capturedPiece = capturedPiece;
        this.promotion = promotion;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public String getUci() {
        return uci;
    }

    public String getSan() {
        return san;
    }

    public String getFenAfterMove() {
        return fenAfterMove;
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
