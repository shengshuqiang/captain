package com.shuqiang.captain.chess.model;

public class ChessGameRecord {
    private String gameId;
    private long startedAt;
    private long endedAt;
    private String myUsername;
    private ChessAvatarType myAvatarType;
    private String myAvatarValue;
    private String opponentUsername;
    private ChessAvatarType opponentAvatarType;
    private String opponentAvatarValue;
    private ChessColor myColor;
    private ChessGameResult result;
    private ChessTerminationReason terminationReason;
    private int moveCount;
    private String finalFen;
    private String pgnText;
    private boolean aborted;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public ChessAvatarType getMyAvatarType() {
        return myAvatarType;
    }

    public void setMyAvatarType(ChessAvatarType myAvatarType) {
        this.myAvatarType = myAvatarType;
    }

    public String getMyAvatarValue() {
        return myAvatarValue;
    }

    public void setMyAvatarValue(String myAvatarValue) {
        this.myAvatarValue = myAvatarValue;
    }

    public String getOpponentUsername() {
        return opponentUsername;
    }

    public void setOpponentUsername(String opponentUsername) {
        this.opponentUsername = opponentUsername;
    }

    public ChessAvatarType getOpponentAvatarType() {
        return opponentAvatarType;
    }

    public void setOpponentAvatarType(ChessAvatarType opponentAvatarType) {
        this.opponentAvatarType = opponentAvatarType;
    }

    public String getOpponentAvatarValue() {
        return opponentAvatarValue;
    }

    public void setOpponentAvatarValue(String opponentAvatarValue) {
        this.opponentAvatarValue = opponentAvatarValue;
    }

    public ChessColor getMyColor() {
        return myColor;
    }

    public void setMyColor(ChessColor myColor) {
        this.myColor = myColor;
    }

    public ChessGameResult getResult() {
        return result;
    }

    public void setResult(ChessGameResult result) {
        this.result = result;
    }

    public ChessTerminationReason getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(ChessTerminationReason terminationReason) {
        this.terminationReason = terminationReason;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public String getFinalFen() {
        return finalFen;
    }

    public void setFinalFen(String finalFen) {
        this.finalFen = finalFen;
    }

    public String getPgnText() {
        return pgnText;
    }

    public void setPgnText(String pgnText) {
        this.pgnText = pgnText;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }
}
