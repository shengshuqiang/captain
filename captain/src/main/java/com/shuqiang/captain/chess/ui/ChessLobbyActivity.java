package com.shuqiang.captain.chess.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shuqiang.captain.chess.bt.ChessBluetoothPermissionManager;
import com.shuqiang.captain.chess.model.ChessConnectionState;
import com.shuqiang.captain.chess.model.ChessMatchSnapshot;
import com.shuqiang.captain.chess.model.ChessScannedDevice;
import com.shuqiang.captain.chess.model.ChessSeatPreference;
import com.shuqiang.captain.chess.model.ChessUserProfile;

import java.util.List;

import captain.R;

public class ChessLobbyActivity extends BaseChessActivity {
    private static final int ACTION_NONE = 0;
    private static final int ACTION_HOST = 1;
    private static final int ACTION_DISCOVER = 2;
    private static final int ACTION_CONNECT = 3;

    private ImageView avatarView;
    private TextView usernameView;
    private TextView statusView;
    private TextView emptyDeviceView;
    private Button hostButton;
    private Button scanButton;
    private Button resumeButton;
    private ChessDeviceAdapter deviceAdapter;
    private int pendingAction = ACTION_NONE;
    private int pendingPermissionMask = ChessBluetoothPermissionManager.PERMISSION_NONE;
    private String pendingDeviceAddress;
    private String launchedGameId;
    private boolean onboardingRedirected;
    private ChessMatchSnapshot latestSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chess_title);
        }
        avatarView = findViewById(R.id.profile_avatar);
        usernameView = findViewById(R.id.profile_username);
        statusView = findViewById(R.id.connection_status_text);
        emptyDeviceView = findViewById(R.id.device_empty_text);
        hostButton = findViewById(R.id.host_button);
        scanButton = findViewById(R.id.scan_button);
        resumeButton = findViewById(R.id.resume_game_button);
        RecyclerView deviceRecyclerView = findViewById(R.id.device_recycler_view);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new ChessDeviceAdapter(device -> {
            pendingDeviceAddress = device.getAddress();
            ensureBluetoothReady(ChessBluetoothPermissionManager.PERMISSION_CONNECT
                    | ChessBluetoothPermissionManager.PERMISSION_SCAN, ACTION_CONNECT);
        });
        deviceRecyclerView.setAdapter(deviceAdapter);
        findViewById(R.id.edit_profile_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChessOnboardingActivity.class);
            intent.putExtra(ChessOnboardingActivity.EXTRA_EDIT_MODE, true);
            startActivity(intent);
        });
        findViewById(R.id.history_button).setOnClickListener(v ->
                startActivity(new Intent(this, ChessHistoryActivity.class)));
        hostButton.setOnClickListener(v -> ensureBluetoothReady(ChessBluetoothPermissionManager.PERMISSION_CONNECT
                | ChessBluetoothPermissionManager.PERMISSION_ADVERTISE, ACTION_HOST));
        scanButton.setOnClickListener(v -> ensureBluetoothReady(ChessBluetoothPermissionManager.PERMISSION_CONNECT
                | ChessBluetoothPermissionManager.PERMISSION_SCAN, ACTION_DISCOVER));
        resumeButton.setOnClickListener(v -> startActivity(new Intent(this, ChessGameActivity.class)));
        applySeatSelection(chessManager.getLastSeatPreference());
        bindProfile(chessManager.getLocalProfile());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!onboardingRedirected && !chessManager.hasCompletedOnboarding()) {
            onboardingRedirected = true;
            startActivity(new Intent(this, ChessOnboardingActivity.class));
            finish();
        }
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_chess_lobby;
    }

    @Override
    public void onSnapshotChanged(ChessMatchSnapshot snapshot) {
        latestSnapshot = snapshot;
        bindProfile(snapshot.getLocalProfile());
        statusView.setText(snapshot.getStatusText());
        hostButton.setEnabled(snapshot.getConnectionState() != ChessConnectionState.PLAYING);
        scanButton.setEnabled(snapshot.getConnectionState() != ChessConnectionState.PLAYING);
        resumeButton.setVisibility(snapshot.getConnectionState() == ChessConnectionState.PLAYING ? View.VISIBLE : View.GONE);
        if (snapshot.getSeatPreference() != null) {
            applySeatSelection(snapshot.getSeatPreference());
        }
        if (!TextUtils.isEmpty(snapshot.getGameId())
                && snapshot.getConnectionState() == ChessConnectionState.PLAYING
                && !snapshot.getGameId().equals(launchedGameId)) {
            launchedGameId = snapshot.getGameId();
            startActivity(new Intent(this, ChessGameActivity.class));
        }
    }

    @Override
    public void onDiscoveredDevicesChanged(List<ChessScannedDevice> devices) {
        deviceAdapter.submitList(devices);
        emptyDeviceView.setVisibility(devices == null || devices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onErrorMessage(String message) {
        super.onErrorMessage(message);
        if (TextUtils.equals(message, "请先完成用户名和头像设置") && !isFinishing()) {
            startActivity(new Intent(this, ChessOnboardingActivity.class));
            finish();
        }
    }

    private void bindProfile(ChessUserProfile profile) {
        if (profile == null) {
            return;
        }
        usernameView.setText(TextUtils.isEmpty(profile.getUsername())
                ? getString(R.string.chess_onboarding_title) : profile.getUsername());
        ChessAvatarLoader.load(avatarView, chessManager.getAvatarStore(), profile);
    }

    private void applySeatSelection(ChessSeatPreference seatPreference) {
        RadioButton whiteButton = findViewById(R.id.radio_white);
        RadioButton blackButton = findViewById(R.id.radio_black);
        RadioButton randomButton = findViewById(R.id.radio_random);
        if (seatPreference == ChessSeatPreference.WHITE) {
            whiteButton.setChecked(true);
        } else if (seatPreference == ChessSeatPreference.BLACK) {
            blackButton.setChecked(true);
        } else {
            randomButton.setChecked(true);
        }
    }

    private ChessSeatPreference getSelectedSeatPreference() {
        if (((RadioButton) findViewById(R.id.radio_white)).isChecked()) {
            return ChessSeatPreference.WHITE;
        }
        if (((RadioButton) findViewById(R.id.radio_black)).isChecked()) {
            return ChessSeatPreference.BLACK;
        }
        return ChessSeatPreference.RANDOM;
    }

    private void ensureBluetoothReady(int permissionMask, int action) {
        if (!chessManager.isBluetoothSupported()) {
            onErrorMessage(getString(R.string.chess_bluetooth_unsupported));
            return;
        }
        pendingAction = action;
        pendingPermissionMask = permissionMask;
        if (ChessBluetoothPermissionManager.requestPermissionsIfNeeded(this, permissionMask)) {
            return;
        }
        continuePendingActionWithBluetoothCheck();
    }

    private void continuePendingActionWithBluetoothCheck() {
        BluetoothAdapter adapter = chessManager.getBluetoothAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            startActivityForResult(ChessBluetoothPermissionManager.createEnableBluetoothIntent(),
                    ChessBluetoothPermissionManager.REQUEST_CODE_ENABLE_BLUETOOTH);
            return;
        }
        if (pendingAction == ACTION_HOST && ChessBluetoothPermissionManager.requiresPermission(
                pendingPermissionMask, ChessBluetoothPermissionManager.PERMISSION_ADVERTISE)) {
            startActivityForResult(ChessBluetoothPermissionManager.createDiscoverableIntent(),
                    ChessBluetoothPermissionManager.REQUEST_CODE_ENABLE_DISCOVERABLE);
            return;
        }
        executePendingAction();
    }

    private void executePendingAction() {
        int action = pendingAction;
        String deviceAddress = pendingDeviceAddress;
        pendingAction = ACTION_NONE;
        pendingPermissionMask = ChessBluetoothPermissionManager.PERMISSION_NONE;
        pendingDeviceAddress = null;
        if (action == ACTION_HOST) {
            chessManager.startHosting(getSelectedSeatPreference());
        } else if (action == ACTION_DISCOVER) {
            chessManager.startDiscovery(getSelectedSeatPreference());
        } else if (action == ACTION_CONNECT && !TextUtils.isEmpty(deviceAddress)) {
            chessManager.connectToDevice(deviceAddress);
        }
    }

    private void clearPendingAction() {
        pendingAction = ACTION_NONE;
        pendingPermissionMask = ChessBluetoothPermissionManager.PERMISSION_NONE;
        pendingDeviceAddress = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ChessBluetoothPermissionManager.REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length == 0) {
                clearPendingAction();
                onErrorMessage(getString(R.string.chess_enable_bluetooth_permissions));
                return;
            }
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    clearPendingAction();
                    onErrorMessage(getString(R.string.chess_enable_bluetooth_permissions));
                    return;
                }
            }
            continuePendingActionWithBluetoothCheck();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChessBluetoothPermissionManager.REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                continuePendingActionWithBluetoothCheck();
            } else {
                clearPendingAction();
                onErrorMessage(getString(R.string.chess_enable_bluetooth));
            }
            return;
        }
        if (requestCode == ChessBluetoothPermissionManager.REQUEST_CODE_ENABLE_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                clearPendingAction();
                onErrorMessage(getString(R.string.chess_enable_discoverable));
                return;
            }
            executePendingAction();
        }
    }
}
