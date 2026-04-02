package com.shuqiang.captain.chess.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.BaseActivity;
import com.shuqiang.captain.chess.ChessManager;
import com.shuqiang.captain.chess.model.ChessGameRecord;

import java.util.List;

import captain.R;

public class ChessHistoryActivity extends BaseActivity {
    private ChessManager chessManager;
    private ChessHistoryAdapter historyAdapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chessManager = ChessManager.getInstance(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chess_history_title);
        }
        emptyView = findViewById(R.id.empty_history_text);
        RecyclerView recyclerView = findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new ChessHistoryAdapter(chessManager.getAvatarStore(), this::openReplay);
        recyclerView.setAdapter(historyAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<ChessGameRecord> records = chessManager.listHistoryGames();
        historyAdapter.submitList(records);
        emptyView.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_chess_history;
    }

    private void openReplay(ChessGameRecord record) {
        Intent intent = ChessReplayActivity.createIntent(this, record.getGameId());
        startActivity(intent);
    }
}
