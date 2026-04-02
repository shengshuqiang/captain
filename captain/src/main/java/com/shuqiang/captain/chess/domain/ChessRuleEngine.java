package com.shuqiang.captain.chess.domain;

import android.text.TextUtils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessGameResult;
import com.shuqiang.captain.chess.model.ChessMoveOutcome;
import com.shuqiang.captain.chess.model.ChessMoveRecord;
import com.shuqiang.captain.chess.model.ChessTerminationReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChessRuleEngine {
    private Board board;
    private MoveList moveList;
    private final List<ChessMoveRecord> moveRecords = new ArrayList<>();

    public ChessRuleEngine() {
        reset();
    }

    public void reset() {
        board = new Board();
        moveList = new MoveList();
        moveRecords.clear();
    }

    public void restoreFromSan(String sanText) throws MoveConversionException {
        reset();
        if (TextUtils.isEmpty(sanText)) {
            return;
        }
        MoveList restoredList = new MoveList();
        restoredList.loadFromSan(sanText);
        Board restoredBoard = new Board();
        List<ChessMoveRecord> restoredMoves = new ArrayList<>();
        for (Move move : restoredList) {
            String san = buildSan(restoredBoard, move);
            if (!restoredBoard.doMove(move, true)) {
                throw new MoveConversionException("恢复棋谱失败: " + move.toString());
            }
            Piece captured = restoredBoard.getBackup().getLast().getCapturedPiece();
            restoredMoves.add(new ChessMoveRecord(restoredMoves.size() + 1, move.toString(), san,
                    restoredBoard.getFen(), ChessColor.fromSide(restoredBoard.getSideToMove()),
                    restoredBoard.isKingAttacked(), captured == Piece.NONE ? null : captured.getFenSymbol(),
                    move.getPromotion() == Piece.NONE ? null : move.getPromotion().getFenSymbol()));
        }
        board = restoredBoard;
        moveList = restoredList;
        moveRecords.clear();
        moveRecords.addAll(restoredMoves);
    }

    public ChessMoveOutcome applyMove(String from, String to, String promotion) {
        ChessMoveOutcome outcome = new ChessMoveOutcome();
        Move move = buildMove(from, to, promotion);
        if (move == null) {
            outcome.setApproved(false);
            outcome.setErrorMessage("走子坐标无效");
            return outcome;
        }
        if (!board.legalMoves().contains(move)) {
            outcome.setApproved(false);
            outcome.setErrorMessage(needsPromotion(from, to) && TextUtils.isEmpty(promotion)
                    ? "兵到底线后需要先选择升变棋子"
                    : "当前走法不合法");
            return outcome;
        }
        try {
            String san = buildSan(board, move);
            if (!board.doMove(move, true)) {
                outcome.setApproved(false);
                outcome.setErrorMessage("规则引擎拒绝了这一步");
                return outcome;
            }
            moveList.add(move);
            Piece captured = board.getBackup().getLast().getCapturedPiece();
            ChessMoveRecord moveRecord = new ChessMoveRecord(moveRecords.size() + 1, move.toString(), san,
                    board.getFen(), ChessColor.fromSide(board.getSideToMove()), board.isKingAttacked(),
                    captured == Piece.NONE ? null : captured.getFenSymbol(),
                    move.getPromotion() == Piece.NONE ? null : move.getPromotion().getFenSymbol());
            moveRecords.add(moveRecord);
            outcome.setApproved(true);
            outcome.setMoveRecord(moveRecord);
            outcome.setPgnText(moveList.toSanWithMoveNumbers().trim());
            populateGameState(outcome);
            return outcome;
        } catch (Exception exception) {
            outcome.setApproved(false);
            outcome.setErrorMessage("走子处理失败: " + exception.getMessage());
            return outcome;
        }
    }

    public boolean isLegalMove(String from, String to, String promotion) {
        Move move = buildMove(from, to, promotion);
        return move != null && board.legalMoves().contains(move);
    }

    public boolean needsPromotion(String from, String to) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            return false;
        }
        try {
            Square fromSquare = Square.valueOf(from.toUpperCase());
            Square toSquare = Square.valueOf(to.toUpperCase());
            Piece piece = board.getPiece(fromSquare);
            if (piece == Piece.NONE || piece.getPieceType() != com.github.bhlangonijr.chesslib.PieceType.PAWN) {
                return false;
            }
            return (piece.getPieceSide() == Side.WHITE && toSquare.getRank() == com.github.bhlangonijr.chesslib.Rank.RANK_8)
                    || (piece.getPieceSide() == Side.BLACK && toSquare.getRank() == com.github.bhlangonijr.chesslib.Rank.RANK_1);
        } catch (Exception ignore) {
            return false;
        }
    }

    public List<String> getLegalTargets(String from) {
        if (TextUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        Square square;
        try {
            square = Square.valueOf(from.toUpperCase());
        } catch (Exception exception) {
            return Collections.emptyList();
        }
        List<String> targets = new ArrayList<>();
        for (Move move : board.legalMoves()) {
            if (move.getFrom() == square) {
                targets.add(move.getTo().toString().toLowerCase());
            }
        }
        return targets;
    }

    public Piece getPiece(String square) {
        try {
            return board.getPiece(Square.valueOf(square.toUpperCase()));
        } catch (Exception exception) {
            return Piece.NONE;
        }
    }

    public String getFen() {
        return board.getFen();
    }

    public String getPgnText() {
        try {
            return moveList.toSanWithMoveNumbers().trim();
        } catch (Exception exception) {
            return "";
        }
    }

    public List<ChessMoveRecord> getMoveRecords() {
        return new ArrayList<>(moveRecords);
    }

    public int getMoveCount() {
        return moveRecords.size();
    }

    public ChessColor getActiveColor() {
        return ChessColor.fromSide(board.getSideToMove());
    }

    public boolean isKingAttacked() {
        return board.isKingAttacked();
    }

    public boolean canClaimThreefoldRepetition() {
        return board.isRepetition(3);
    }

    public boolean isFivefoldRepetitionAuto() {
        return board.isRepetition(5);
    }

    public boolean canClaimFiftyMoveDraw() {
        return board.getHalfMoveCounter() >= 100;
    }

    public boolean isSeventyFiveMoveAuto() {
        return board.getHalfMoveCounter() >= 150;
    }

    public boolean isInsufficientMaterial() {
        return board.isInsufficientMaterial();
    }

    public ChessMoveOutcome claimDrawIfPossible() {
        ChessMoveOutcome outcome = new ChessMoveOutcome();
        if (isFivefoldRepetitionAuto()) {
            outcome.setApproved(true);
            outcome.setFinished(true);
            outcome.setResult(ChessGameResult.DRAW);
            outcome.setTerminationReason(ChessTerminationReason.FIVEFOLD_REPETITION_AUTO);
        } else if (isSeventyFiveMoveAuto()) {
            outcome.setApproved(true);
            outcome.setFinished(true);
            outcome.setResult(ChessGameResult.DRAW);
            outcome.setTerminationReason(ChessTerminationReason.SEVENTY_FIVE_MOVE_AUTO);
        } else if (canClaimThreefoldRepetition()) {
            outcome.setApproved(true);
            outcome.setFinished(true);
            outcome.setResult(ChessGameResult.DRAW);
            outcome.setTerminationReason(ChessTerminationReason.THREEFOLD_REPETITION_CLAIM);
        } else if (canClaimFiftyMoveDraw()) {
            outcome.setApproved(true);
            outcome.setFinished(true);
            outcome.setResult(ChessGameResult.DRAW);
            outcome.setTerminationReason(ChessTerminationReason.FIFTY_MOVE_CLAIM);
        } else {
            outcome.setApproved(false);
            outcome.setErrorMessage("当前局面暂不支持按规则申请和棋");
        }
        outcome.setPgnText(getPgnText());
        return outcome;
    }

    private Move buildMove(String from, String to, String promotion) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            return null;
        }
        try {
            return new Move(from.toLowerCase() + to.toLowerCase() + normalizePromotion(promotion), board.getSideToMove());
        } catch (Exception exception) {
            return null;
        }
    }

    private String normalizePromotion(String promotion) {
        if (TextUtils.isEmpty(promotion)) {
            return "";
        }
        return promotion.substring(0, 1).toLowerCase();
    }

    private String buildSan(Board currentBoard, Move move) throws MoveConversionException {
        MoveList singleMoveList = new MoveList(currentBoard.getFen());
        singleMoveList.add(move);
        return singleMoveList.toSanArray()[0];
    }

    private void populateGameState(ChessMoveOutcome outcome) {
        outcome.setCanClaimDraw(false);
        outcome.setClaimableDrawReason(null);
        if (board.isMated()) {
            outcome.setFinished(true);
            outcome.setTerminationReason(ChessTerminationReason.CHECKMATE);
            outcome.setResult(board.getSideToMove() == Side.WHITE ? ChessGameResult.BLACK_WIN : ChessGameResult.WHITE_WIN);
            return;
        }
        if (board.isStaleMate()) {
            outcome.setFinished(true);
            outcome.setTerminationReason(ChessTerminationReason.STALEMATE);
            outcome.setResult(ChessGameResult.DRAW);
            return;
        }
        if (board.isInsufficientMaterial()) {
            outcome.setFinished(true);
            outcome.setTerminationReason(ChessTerminationReason.INSUFFICIENT_MATERIAL);
            outcome.setResult(ChessGameResult.DRAW);
            return;
        }
        if (isFivefoldRepetitionAuto()) {
            outcome.setFinished(true);
            outcome.setTerminationReason(ChessTerminationReason.FIVEFOLD_REPETITION_AUTO);
            outcome.setResult(ChessGameResult.DRAW);
            return;
        }
        if (isSeventyFiveMoveAuto()) {
            outcome.setFinished(true);
            outcome.setTerminationReason(ChessTerminationReason.SEVENTY_FIVE_MOVE_AUTO);
            outcome.setResult(ChessGameResult.DRAW);
            return;
        }
        if (canClaimThreefoldRepetition()) {
            outcome.setCanClaimDraw(true);
            outcome.setClaimableDrawReason(ChessTerminationReason.THREEFOLD_REPETITION_CLAIM);
            return;
        }
        if (canClaimFiftyMoveDraw()) {
            outcome.setCanClaimDraw(true);
            outcome.setClaimableDrawReason(ChessTerminationReason.FIFTY_MOVE_CLAIM);
        }
    }
}
