package com.shuqiang.captain.chess.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.shuqiang.captain.chess.model.ChessMessage;
import com.shuqiang.captain.chess.model.ChessScannedDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import captain.R;

public class ChessBluetoothService {
    private static final String SERVICE_NAME = "CaptainChess";
    private static final UUID SERVICE_UUID = UUID.fromString("8f2d48f7-7e1a-4b5b-8e13-6f6c6b5dfab4");

    public interface Listener {
        void onDiscoveryDevicesChanged(List<ChessScannedDevice> devices);
        void onSocketConnected(boolean asHost, String remoteDeviceName);
        void onMessageReceived(ChessMessage message);
        void onConnectionLost(String reason);
        void onError(String errorMessage);
    }

    private final Context appContext;
    private final Listener listener;
    private final ChessBluetoothProtocol protocol = new ChessBluetoothProtocol();
    private final Map<String, ChessScannedDevice> scannedDeviceMap = new LinkedHashMap<>();
    private final BluetoothAdapter bluetoothAdapter;
    private boolean receiverRegistered;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothSocket activeSocket;
    private String remoteDeviceName;
    private boolean suppressConnectionLostCallback;
    private boolean transportFailureHandled;

    public ChessBluetoothService(Context context, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public String getRemoteDeviceName() {
        return remoteDeviceName;
    }

    public boolean isConnected() {
        return activeSocket != null && activeSocket.isConnected();
    }

    public void startDiscovery() {
        if (bluetoothAdapter == null) {
            listener.onError("当前设备不支持蓝牙");
            return;
        }
        if (!ensurePermissions(ChessBluetoothPermissionManager.PERMISSION_CONNECT
                | ChessBluetoothPermissionManager.PERMISSION_SCAN)) {
            return;
        }
        registerReceiverIfNeeded();
        scannedDeviceMap.clear();
        cancelDiscoverySafely();
        try {
            for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
                addDevice(bondedDevice);
            }
            if (!bluetoothAdapter.startDiscovery()) {
                listener.onError("蓝牙搜索启动失败");
                dispatchDevices();
            }
        } catch (SecurityException exception) {
            dispatchPermissionError();
            dispatchDevices();
        }
    }

    public void stopDiscovery() {
        cancelDiscoverySafely();
        unregisterReceiverIfNeeded();
    }

    public List<ChessScannedDevice> getDiscoveredDevices() {
        return new ArrayList<>(scannedDeviceMap.values());
    }

    public void startHosting() {
        if (bluetoothAdapter == null) {
            listener.onError("当前设备不支持蓝牙");
            return;
        }
        if (!ensurePermissions(ChessBluetoothPermissionManager.PERMISSION_CONNECT)) {
            return;
        }
        disconnect();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void connect(String address) {
        if (bluetoothAdapter == null) {
            listener.onError("当前设备不支持蓝牙");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            listener.onError("未选择可连接的设备");
            return;
        }
        if (!ensurePermissions(ChessBluetoothPermissionManager.PERMISSION_CONNECT
                | ChessBluetoothPermissionManager.PERMISSION_SCAN)) {
            return;
        }
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            cancelDiscoverySafely();
            closeConnectThread();
            connectThread = new ConnectThread(device);
            connectThread.start();
        } catch (SecurityException exception) {
            dispatchPermissionError();
        } catch (Exception exception) {
            listener.onError("连接设备失败: " + exception.getMessage());
        }
    }

    public synchronized void sendMessage(ChessMessage message) {
        if (connectedThread == null) {
            listener.onError("蓝牙连接尚未建立");
            return;
        }
        connectedThread.write(message);
    }

    public synchronized void disconnect() {
        suppressConnectionLostCallback = true;
        transportFailureHandled = true;
        stopDiscovery();
        closeAcceptThread();
        closeConnectThread();
        closeConnectedThread();
        closeSocket(activeSocket);
        activeSocket = null;
        remoteDeviceName = null;
    }

    private void registerReceiverIfNeeded() {
        if (receiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        appContext.registerReceiver(discoveryReceiver, filter);
        receiverRegistered = true;
    }

    private void unregisterReceiverIfNeeded() {
        if (!receiverRegistered) {
            return;
        }
        try {
            appContext.unregisterReceiver(discoveryReceiver);
        } catch (Exception ignore) {
        }
        receiverRegistered = false;
    }

    private void addDevice(BluetoothDevice device) {
        if (device == null || TextUtils.isEmpty(device.getAddress())) {
            return;
        }
        String name = "附近设备";
        try {
            name = device.getName();
        } catch (SecurityException ignore) {
        }
        if (TextUtils.isEmpty(name)) {
            name = "附近设备";
        }
        scannedDeviceMap.put(device.getAddress(), new ChessScannedDevice(name, device.getAddress(),
                isBondedDevice(device)));
        dispatchDevices();
    }

    private void dispatchDevices() {
        listener.onDiscoveryDevicesChanged(new ArrayList<>(scannedDeviceMap.values()));
    }

    private synchronized void onSocketReady(BluetoothSocket socket, boolean asHost) {
        closeAcceptThread();
        closeConnectThread();
        closeConnectedThread();
        activeSocket = socket;
        remoteDeviceName = getRemoteDeviceName(socket);
        suppressConnectionLostCallback = false;
        transportFailureHandled = false;
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        listener.onSocketConnected(asHost, remoteDeviceName);
    }

    private synchronized void handleTransportFailure(BluetoothSocket sourceSocket, String message) {
        if (sourceSocket != null && activeSocket != null && sourceSocket != activeSocket) {
            return;
        }
        if (transportFailureHandled) {
            return;
        }
        transportFailureHandled = true;
        closeConnectedThread();
        closeSocket(activeSocket);
        activeSocket = null;
        remoteDeviceName = null;
        if (!suppressConnectionLostCallback && !TextUtils.isEmpty(message)) {
            listener.onConnectionLost(message);
        }
    }

    private void closeAcceptThread() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    private void closeConnectThread() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    private void closeConnectedThread() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    private boolean ensurePermissions(int permissionMask) {
        if (ChessBluetoothPermissionManager.hasRequiredPermissions(appContext, permissionMask)) {
            return true;
        }
        dispatchPermissionError();
        return false;
    }

    private void cancelDiscoverySafely() {
        if (bluetoothAdapter == null
                || !ChessBluetoothPermissionManager.hasRequiredPermissions(appContext,
                ChessBluetoothPermissionManager.PERMISSION_SCAN)) {
            return;
        }
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (SecurityException ignore) {
        }
    }

    private boolean isBondedDevice(BluetoothDevice device) {
        try {
            return device.getBondState() == BluetoothDevice.BOND_BONDED;
        } catch (SecurityException ignore) {
            return false;
        }
    }

    private String getRemoteDeviceName(BluetoothSocket socket) {
        if (socket == null || socket.getRemoteDevice() == null) {
            return "";
        }
        try {
            String deviceName = socket.getRemoteDevice().getName();
            return TextUtils.isEmpty(deviceName) ? "" : deviceName;
        } catch (SecurityException ignore) {
            return "";
        }
    }

    private void dispatchPermissionError() {
        listener.onError(appContext.getString(R.string.chess_enable_bluetooth_permissions));
    }

    private void closeSocket(BluetoothSocket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDevice(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)
                    || BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                dispatchDevices();
            }
        }
    };

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private volatile boolean cancelled;

        AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            } catch (SecurityException exception) {
                dispatchPermissionError();
            } catch (IOException exception) {
                listener.onError("Host 监听创建失败: " + exception.getMessage());
            }
        }

        @Override
        public void run() {
            if (serverSocket == null) {
                return;
            }
            try {
                BluetoothSocket socket = serverSocket.accept();
                if (socket != null) {
                    onSocketReady(socket, true);
                }
            } catch (IOException exception) {
                if (!cancelled) {
                    listener.onError("等待对手连接失败: " + exception.getMessage());
                }
            } finally {
                cancel();
            }
        }

        void cancel() {
            cancelled = true;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;
        private volatile boolean cancelled;

        ConnectThread(BluetoothDevice device) {
            this.device = device;
            try {
                socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            } catch (SecurityException exception) {
                dispatchPermissionError();
            } catch (IOException exception) {
                listener.onError("创建连接通道失败: " + exception.getMessage());
            }
        }

        @Override
        public void run() {
            if (socket == null) {
                return;
            }
            try {
                socket.connect();
                onSocketReady(socket, false);
            } catch (SecurityException exception) {
                closeSocket(socket);
                if (!cancelled) {
                    dispatchPermissionError();
                }
            } catch (IOException exception) {
                closeSocket(socket);
                if (!cancelled) {
                    listener.onError("连接对手失败: " + exception.getMessage());
                }
            }
        }

        void cancel() {
            cancelled = true;
            closeSocket(socket);
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;

        ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted() && socket.isConnected()) {
                    ChessMessage message = protocol.readMessage(socket.getInputStream());
                    if (message == null) {
                        break;
                    }
                    listener.onMessageReceived(message);
                }
            } catch (IOException exception) {
                handleTransportFailure(socket, "蓝牙链路已断开");
                return;
            }
            handleTransportFailure(socket, "蓝牙链路已断开");
        }

        void write(ChessMessage message) {
            try {
                protocol.writeMessage(socket.getOutputStream(), message);
            } catch (IOException exception) {
                handleTransportFailure(socket, "蓝牙消息发送失败");
            }
        }

        void cancel() {
            closeSocket(socket);
            interrupt();
        }
    }
}
