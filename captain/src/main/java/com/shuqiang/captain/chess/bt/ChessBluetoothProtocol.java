package com.shuqiang.captain.chess.bt;

import com.google.gson.Gson;
import com.shuqiang.captain.chess.model.ChessMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ChessBluetoothProtocol {
    private static final int MAX_MESSAGE_BYTES = 128 * 1024;
    private final Gson gson = new Gson();

    public synchronized void writeMessage(OutputStream outputStream, ChessMessage message) throws IOException {
        byte[] data = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_MESSAGE_BYTES) {
            throw new IOException("蓝牙消息过大");
        }
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(data.length);
        dataOutputStream.write(data);
        dataOutputStream.flush();
    }

    public ChessMessage readMessage(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int size;
        try {
            size = dataInputStream.readInt();
        } catch (EOFException eofException) {
            return null;
        }
        if (size <= 0 || size > MAX_MESSAGE_BYTES) {
            throw new IOException("蓝牙消息长度非法");
        }
        byte[] data = new byte[size];
        dataInputStream.readFully(data);
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), ChessMessage.class);
    }
}
