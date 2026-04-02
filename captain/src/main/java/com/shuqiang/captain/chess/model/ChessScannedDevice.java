package com.shuqiang.captain.chess.model;

public class ChessScannedDevice {
    private final String name;
    private final String address;
    private final boolean bonded;

    public ChessScannedDevice(String name, String address, boolean bonded) {
        this.name = name;
        this.address = address;
        this.bonded = bonded;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isBonded() {
        return bonded;
    }
}
