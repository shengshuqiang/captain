package com.shuqiang.captain.chess.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.bhlangonijr.chesslib.Piece;
import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessConnectionState;
import com.shuqiang.captain.chess.model.ChessMatchSnapshot;

import java.util.Collections;
import java.util.List;

import captain.R;

public class ChessGameActivity extends BaseChessActivity {
    private ImageView opponentAvatarView;
    private TextView opponentNameView;
    private TextView opponentColorView;
    private ImageView myAvatarView;
    private TextView myNameView;
    private TextView myColorView;
    private TextView statusView;
    private TextView moveView;
    private Button drawButton;
    private Button resignButton;
    private Button replayButton;
    private Button flipButton;
    private ChessBoardView boardView;
    private ChessMatchSnapshot latestSnapshot;
    private String selectedSquare;
    private boolean drawDialogShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chess_game_title);
        }
        opponentAvatarView = findViewById(R.id.opponent_avatar);
        opponentNameView = findViewById(R.id.opponent_name);
        opponentColorView = findViewById(R.id.opponent_color);
        myAvatarView = findViewById(R.id.my_avatar);
        myNameView = findViewById(R.id.my_name);
        myColorView = findViewById(R.id.my_color);
        statusView = findViewById(R.id.game_status_text);
        moveView = findViewById(R.id.last_move_text);
        drawButton = findViewById(R.id.draw_button);
        resignButton = findViewById(R.id.resign_button);
        replayButton = findViewById(R.id.replay_button);
        flipButton = findViewById(R.id.flip_button);
        boardView = findViewById(R.id.chess_board_view);
        boardView.setOnSquareTapListener(this::handleSquareTap);
        drawButton.setOnClickListener(v -> chessManager.offerDraw());
        resignButton.setOnClickListener(v -> showResignDialog());
        flipButton.setOnClickListener(v -> boardView.setFlipped(!boardView.isFlipped()));
        replayButton.setOnClickListener(v -> {
            if (latestSnapshot != null && !TextUtils.isEmpty(latestSnapshot.getGameId())) {
                startActivity(ChessReplayActivity.createIntent(this, latestSnapshot.getGameId()));
            }
        });
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_chess_game;
    }

    @Override
    public void onSnapshotChanged(ChessMatchSnapshot snapshot) {
        latestSnapshot = snapshot;
        if (snapshot.getConnectionState() == ChessConnectionState.IDLE) {
            finish();
            return;
        }
        ChessAvatarLoader.load(opponentAvatarView, chessManager.getAvatarStore(), snapshot.getOpponentProfile());
        ChessAvatarLoader.load(myAvatarView, chessManager.getAvatarStore(), snapshot.getLocalProfile());
        opponentNameView.setText(snapshot.getOpponentProfile() == null || TextUtils.isEmpty(snapshot.getOpponentProfile().getUsername())
                ? getString(R.string.chess_unknown_player) : snapshot.getOpponentProfile().getUsername());
        myNameView.setText(snapshot.getLocalProfile() == null ? "" : snapshot.getLocalProfile().getUsername());
        myColorView.setText(snapshot.getMyColor() == null ? "" : snapshot.getMyColor().getLabel());
        opponentColorView.setText(snapshot.getMyColor() == null ? "" : snapshot.getMyColor().flip().getLabel());
        statusView.setText(snapshot.getStatusText());
        moveView.setText(snapshot.getMoveRecords().isEmpty()
                ? "开局局面，等待第一步"
                : snapshot.getMoveRecords().get(snapshot.getMoveRecords().size() - 1).getSan());
        drawButton.setText(snapshot.isCanClaimDraw() ? R.string.chess_claim_draw_action : R.string.chess_draw_action);
        drawButton.setEnabled(!snapshot.isFinished());
        resignButton.setEnabled(!snapshot.isFinished());
        replayButton.setVisibility(snapshot.isFinished() ? android.view.View.VISIBLE : android.view.View.GONE);
        boardView.setFen(snapshot.getCurrentFen());
        boardView.setLastMoveUci(snapshot.getLastMoveUci());
        if (!snapshot.isMyTurn()) {
            clearSelection();
        } else if (!TextUtils.isEmpty(selectedSquare)) {
            boardView.setLegalTargets(chessManager.getLegalTargets(selectedSquare));
        }
    }

    @Override
    public void onIncomingDrawOffer(com.shuqiang.captain.chess.model.ChessTerminationReason claimReason) {
        if (drawDialogShowing || isFinishing()) {
            return;
        }
        drawDialogShowing = true;
        new AlertDialog.Builder(this)
                .setTitle(R.string.chess_accept_draw_title)
                .setMessage(R.string.chess_accept_draw_message)
                .setPositiveButton(R.string.chess_accept_draw_positive, (dialog, which) -> {
                    drawDialogShowing = false;
                    chessManager.respondToDrawOffer(true);
                })
                .setNegativeButton(R.string.chess_accept_draw_negative, (dialog, which) -> {
                    drawDialogShowing = false;
                    chessManager.respondToDrawOffer(false);
                })
                .setOnDismissListener(dialog -> drawDialogShowing = false)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (latestSnapshot != null && latestSnapshot.getConnectionState() == ChessConnectionState.PLAYING
                && !latestSnapshot.isFinished()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.chess_exit_game_title)
                    .setMessage(R.string.chess_exit_game_message)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        chessManager.cancelSession();
                        ChessGameActivity.super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        super.onBackPressed();
    }

    private void handleSquareTap(String square) {
        if (latestSnapshot == null || latestSnapshot.isFinished() || !latestSnapshot.isMyTurn()) {
            return;
        }
        if (TextUtils.isEmpty(selectedSquare)) {
            if (isMyPiece(square)) {
                selectSquare(square);
            }
            return;
        }
        if (square.equals(selectedSquare)) {
            clearSelection();
            return;
        }
        List<String> legalTargets = chessManager.getLegalTargets(selectedSquare);
        if (legalTargets.contains(square)) {
            if (chessManager.needsPromotion(selectedSquare, square)) {
                showPromotionDialog(square);
            } else {
                chessManager.submitMove(selectedSquare, square, null);
                clearSelection();
            }
            return;
        }
        if (isMyPiece(square)) {
            selectSquare(square);
        } else {
            clearSelection();
        }
    }

    private void showPromotionDialog(String toSquare) {
        String[] labels = new String[]{
                getString(R.string.chess_piece_queen),
                getString(R.string.chess_piece_rook),
                getString(R.string.chess_piece_bishop),
                getString(R.string.chess_piece_knight)
        };
        String[] values = new String[]{"q", "r", "b", "n"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.chess_promotion_title)
                .setItems(labels, (dialog, which) -> {
                    chessManager.submitMove(selectedSquare, toSquare, values[which]);
                    clearSelection();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showResignDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.chess_resign_confirm_title)
                .setMessage(R.string.chess_resign_confirm_message)
                .setPositiveButton(R.string.chess_resign_confirm_positive, (dialog, which) -> chessManager.resign())
                .setNegativeButton(R.string.chess_resign_confirm_negative, null)
                .show();
    }

    private void selectSquare(String square) {
        selectedSquare = square;
        boardView.setSelectedSquare(square);
        boardView.setLegalTargets(chessManager.getLegalTargets(square));
    }

    private void clearSelection() {
        selectedSquare = null;
        boardView.setSelectedSquare(null);
        boardView.setLegalTargets(Collections.emptyList());
    }

    private boolean isMyPiece(String square) {
        Piece piece = chessManager.getPieceAtSquare(square);
        return latestSnapshot != null
                && piece != null
                && piece != Piece.NONE
                && latestSnapshot.getMyColor() != null
                && piece.getPieceSide() == latestSnapshot.getMyColor().toSide();
    }
}
