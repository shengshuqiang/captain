package com.shuqiang.captain.chess.model;

import java.util.ArrayList;
import java.util.List;

public class ChessMatchSnapshot {
    private ChessConnectionState connectionState = ChessConnectionState.IDLE;
    private ChessSessionRole role;
    private ChessUserProfile localProfile;
    private ChessUserProfile opponentProfile;
    private ChessSeatPreference seatPreference = ChessSeatPreference.RANDOM;
    private ChessColor myColor;
    private String gameId;
    private String currentFen;
    private String pgnText;
    private String statusText;
    private String remoteDeviceName;
    private boolean myTurn;
    private boolean inCheck;
    private boolean finished;
    private boolean waitingForMoveCommit;
    private boolean incomingDrawOffer;
    private boolean outgoingDrawOfferPending;
    private boolean canClaimDraw;
    private ChessTerminationReason claimableDrawReason;
    private ChessGameResult result;
    private ChessTerminationReason terminationReason;
    private String lastMoveUci;
    private int moveCount;
    private final List<ChessMoveRecord> moveRecords = new ArrayList<>();

    public ChessMatchSnapshot copy() {
        ChessMatchSnapshot copy = new ChessMatchSnapshot();
        copy.connectionState = connectionState;
        copy.role = role;
        copy.localProfile = localProfile == null ? null : localProfile.copy();
        copy.opponentProfile = opponentProfile == null ? null : opponentProfile.copy();
        copy.seatPreference = seatPreference;
        copy.myColor = myColor;
        copy.gameId = gameId;
        copy.currentFen = currentFen;
        copy.pgnText = pgnText;
        copy.statusText = statusText;
        copy.remoteDeviceName = remoteDeviceName;
        copy.myTurn = myTurn;
        copy.inCheck = inCheck;
        copy.finished = finished;
        copy.waitingForMoveCommit = waitingForMoveCommit;
        copy.incomingDrawOffer = incomingDrawOffer;
        copy.outgoingDrawOfferPending = outgoingDrawOfferPending;
        copy.canClaimDraw = canClaimDraw;
        copy.claimableDrawReason = claimableDrawReason;
        copy.result = result;
        copy.terminationReason = terminationReason;
        copy.lastMoveUci = lastMoveUci;
        copy.moveCount = moveCount;
        copy.moveRecords.addAll(moveRecords);
        return copy;
    }

    public ChessConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ChessConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ChessSessionRole getRole() {
        return role;
    }

    public void setRole(ChessSessionRole role) {
        this.role = role;
    }

    public ChessUserProfile getLocalProfile() {
        return localProfile;
    }

    public void setLocalProfile(ChessUserProfile localProfile) {
        this.localProfile = localProfile;
    }

    public ChessUserProfile getOpponentProfile() {
        return opponentProfile;
    }

    public void setOpponentProfile(ChessUserProfile opponentProfile) {
        this.opponentProfile = opponentProfile;
    }

    public ChessSeatPreference getSeatPreference() {
        return seatPreference;
    }

    public void setSeatPreference(ChessSeatPreference seatPreference) {
        this.seatPreference = seatPreference;
    }

    public ChessColor getMyColor() {
        return myColor;
    }

    public void setMyColor(ChessColor myColor) {
        this.myColor = myColor;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void setCurrentFen(String currentFen) {
        this.currentFen = currentFen;
    }

    public String getPgnText() {
        return pgnText;
    }

    public void setPgnText(String pgnText) {
        this.pgnText = pgnText;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getRemoteDeviceName() {
        return remoteDeviceName;
    }

    public void setRemoteDeviceName(String remoteDeviceName) {
        this.remoteDeviceName = remoteDeviceName;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public boolean isInCheck() {
        return inCheck;
    }

    public void setInCheck(boolean inCheck) {
        this.inCheck = inCheck;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isWaitingForMoveCommit() {
        return waitingForMoveCommit;
    }

    public void setWaitingForMoveCommit(boolean waitingForMoveCommit) {
        this.waitingForMoveCommit = waitingForMoveCommit;
    }

    public boolean isIncomingDrawOffer() {
        return incomingDrawOffer;
    }

    public void setIncomingDrawOffer(boolean incomingDrawOffer) {
        this.incomingDrawOffer = incomingDrawOffer;
    }

    public boolean isOutgoingDrawOfferPending() {
        return outgoingDrawOfferPending;
    }

    public void setOutgoingDrawOfferPending(boolean outgoingDrawOfferPending) {
        this.outgoingDrawOfferPending = outgoingDrawOfferPending;
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

    public String getLastMoveUci() {
        return lastMoveUci;
    }

    public void setLastMoveUci(String lastMoveUci) {
        this.lastMoveUci = lastMoveUci;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public List<ChessMoveRecord> getMoveRecords() {
        return moveRecords;
    }
}
