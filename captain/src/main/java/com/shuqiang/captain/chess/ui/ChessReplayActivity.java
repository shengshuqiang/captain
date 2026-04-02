package com.shuqiang.captain.chess.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.captain.base.BaseActivity;
import com.shuqiang.captain.chess.ChessManager;
import com.shuqiang.captain.chess.ChessTextFormatter;
import com.shuqiang.captain.chess.model.ChessGameRecord;
import com.shuqiang.captain.chess.model.ChessReplayFrame;

import java.util.Collections;
import java.util.List;

import captain.R;

public class ChessReplayActivity extends BaseActivity {
    private static final String EXTRA_GAME_ID = "game_id";
    private ChessBoardView boardView;
    private TextView summaryView;
    private TextView sanView;
    private TextView stepView;
    private Button startButton;
    private Button prevButton;
    private Button nextButton;
    private Button endButton;
    private Button flipButton;
    private List<ChessReplayFrame> frames = Collections.emptyList();
    private ChessGameRecord record;
    private int currentIndex;

    public static Intent createIntent(Context context, String gameId) {
        Intent intent = new Intent(context, ChessReplayActivity.class);
        intent.putExtra(EXTRA_GAME_ID, gameId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chess_replay_title);
        }
        boardView = findViewById(R.id.replay_board_view);
        summaryView = findViewById(R.id.replay_summary_text);
        sanView = findViewById(R.id.replay_san_text);
        stepView = findViewById(R.id.replay_step_text);
        startButton = findViewById(R.id.replay_start_button);
        prevButton = findViewById(R.id.replay_prev_button);
        nextButton = findViewById(R.id.replay_next_button);
        endButton = findViewById(R.id.replay_end_button);
        flipButton = findViewById(R.id.replay_flip_button);
        startButton.setOnClickListener(v -> {
            currentIndex = 0;
            updateFrame();
        });
        prevButton.setOnClickListener(v -> {
            currentIndex = Math.max(0, currentIndex - 1);
            updateFrame();
        });
        nextButton.setOnClickListener(v -> {
            currentIndex = Math.min(frames.size() - 1, currentIndex + 1);
            updateFrame();
        });
        endButton.setOnClickListener(v -> {
            currentIndex = Math.max(0, frames.size() - 1);
            updateFrame();
        });
        flipButton.setOnClickListener(v -> boardView.setFlipped(!boardView.isFlipped()));
        loadReplay();
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_chess_replay;
    }

    private void loadReplay() {
        ChessManager manager = ChessManager.getInstance(this);
        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (TextUtils.isEmpty(gameId)) {
            Toast.makeText(this, "缺少对局记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        record = manager.findHistoryGame(gameId);
        if (record == null) {
            Toast.makeText(this, "未找到对局记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            frames = manager.buildReplayFrames(record);
            updateFrame();
        } catch (Exception exception) {
            Toast.makeText(this, "复盘构建失败: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateFrame() {
        if (frames.isEmpty()) {
            return;
        }
        ChessReplayFrame frame = frames.get(currentIndex);
        boardView.setFen(frame.getFen());
        boardView.setSelectedSquare(null);
        boardView.setLegalTargets(Collections.emptyList());
        boardView.setLastMoveUci(null);
        summaryView.setText(record.getOpponentUsername() + " · " +
                ChessTextFormatter.combineResultAndReason(record.getResult(), record.getTerminationReason(),
                        record.getMyColor()));
        sanView.setText(currentIndex == 0 ? "开局局面" : frame.getSan());
        stepView.setText(getString(R.string.chess_replay_step, currentIndex, Math.max(0, frames.size() - 1)));
        startButton.setEnabled(currentIndex > 0);
        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < frames.size() - 1);
        endButton.setEnabled(currentIndex < frames.size() - 1);
    }
}
