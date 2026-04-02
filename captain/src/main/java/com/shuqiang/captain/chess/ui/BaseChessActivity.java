package com.shuqiang.captain.chess.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.captain.base.BaseActivity;
import com.shuqiang.captain.chess.ChessManager;
import com.shuqiang.captain.chess.model.ChessMatchSnapshot;
import com.shuqiang.captain.chess.model.ChessScannedDevice;
import com.shuqiang.captain.chess.model.ChessTerminationReason;

import java.util.List;

public abstract class BaseChessActivity extends BaseActivity implements ChessManager.Listener {
    protected ChessManager chessManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chessManager = ChessManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        chessManager.registerListener(this);
    }

    @Override
    protected void onStop() {
        chessManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onSnapshotChanged(ChessMatchSnapshot snapshot) {
    }

    @Override
    public void onDiscoveredDevicesChanged(List<ChessScannedDevice> devices) {
    }

    @Override
    public void onIncomingDrawOffer(ChessTerminationReason claimReason) {
    }

    @Override
    public void onErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
