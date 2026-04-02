package com.shuqiang.captain.chess.model;

public class ChessMoveOutcome {
    private boolean approved;
    private String errorMessage;
    private ChessMoveRecord moveRecord;
    private String pgnText;
    private boolean finished;
    private ChessGameResult result;
    private ChessTerminationReason terminationReason;
    private boolean canClaimDraw;
    private ChessTerminationReason claimableDrawReason;

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ChessMoveRecord getMoveRecord() {
        return moveRecord;
    }

    public void setMoveRecord(ChessMoveRecord moveRecord) {
        this.moveRecord = moveRecord;
    }

    public String getPgnText() {
        return pgnText;
    }

    public void setPgnText(String pgnText) {
        this.pgnText = pgnText;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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

    public boolean isCanClaimDraw() {
        return canClaimDraw;
    }

    public void setCanClaimDraw(boolean canClaimDraw) {
        this.canClaimDraw = canClaimDraw;
    }

    public ChessTerminationReason getClaimableDrawReason() {
        return claimableDrawReason;
    }

    public void setClaimableDrawReason(ChessTerminationReason claimableDrawReason) {
        this.claimableDrawReason = claimableDrawReason;
    }
}
