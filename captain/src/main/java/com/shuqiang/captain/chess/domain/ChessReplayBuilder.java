package com.shuqiang.captain.chess.domain;

import android.text.TextUtils;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessReplayFrame;

import java.util.ArrayList;
import java.util.List;

public class ChessReplayBuilder {
    public List<ChessReplayFrame> buildFrames(String sanText) throws MoveConversionException {
        List<ChessReplayFrame> frames = new ArrayList<>();
        Board board = new Board();
        frames.add(new ChessReplayFrame(0, "", board.getFen(), ChessColor.fromSide(board.getSideToMove()),
                false, null, null));
        if (TextUtils.isEmpty(sanText)) {
            return frames;
        }
        MoveList moveList = new MoveList();
        moveList.loadFromSan(sanText);
        for (Move move : moveList) {
            String san = move.getSan();
            if (!board.doMove(move, true)) {
                throw new MoveConversionException("复盘重放失败: " + move.toString());
            }
            Piece captured = board.getBackup().getLast().getCapturedPiece();
            frames.add(new ChessReplayFrame(frames.size(), san, board.getFen(),
                    ChessColor.fromSide(board.getSideToMove()), board.isKingAttacked(),
                    captured == Piece.NONE ? null : captured.getFenSymbol(),
                    move.getPromotion() == Piece.NONE ? null : move.getPromotion().getFenSymbol()));
        }
        return frames;
    }
}
