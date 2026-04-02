package com.shuqiang.captain.chess.model;

public class ChessMessage {
    public ChessMessageType type;
    public String protocolVersion;
    public String gameId;
    public String username;
    public String avatarType;
    public String avatar;
    public String seatPreference;
    public String deviceName;
    public String hostColor;
    public String peerColor;
    public String statusMessage;
    public String from;
    public String to;
    public String promotion;
    public Integer clientMoveIndex;
    public Boolean approved;
    public String uci;
    public String san;
    public String fen;
    public String pgn;
    public Integer moveIndex;
    public String activeColor;
    public String result;
    public String terminationReason;
    public Boolean accepted;
}
