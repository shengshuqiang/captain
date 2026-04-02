package com.shuqiang.captain.chess;

import android.text.TextUtils;

import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessGameResult;
import com.shuqiang.captain.chess.model.ChessTerminationReason;

public final class ChessTextFormatter {
    private ChessTextFormatter() {
    }

    public static String formatTerminationReason(ChessTerminationReason reason) {
        if (reason == null) {
            return "";
        }
        switch (reason) {
            case CHECKMATE:
                return "将死";
            case STALEMATE:
                return "逼和";
            case DRAW_AGREEMENT:
                return "双方同意和棋";
            case THREEFOLD_REPETITION_CLAIM:
                return "三次重复申请和棋";
            case FIVEFOLD_REPETITION_AUTO:
                return "五次重复自动和棋";
            case FIFTY_MOVE_CLAIM:
                return "50 步规则申请和棋";
            case SEVENTY_FIVE_MOVE_AUTO:
                return "75 步规则自动和棋";
            case INSUFFICIENT_MATERIAL:
                return "子力不足和棋";
            case RESIGNATION:
                return "认输";
            case ABORTED:
                return "连接中止";
            default:
                return reason.name();
        }
    }

    public static String formatResult(ChessGameResult result, ChessColor myColor) {
        if (result == null) {
            return "";
        }
        switch (result) {
            case DRAW:
                return "和棋";
            case ABORTED:
                return "中止";
            case WHITE_WIN:
                return myColor == ChessColor.WHITE ? "你赢了" : "你输了";
            case BLACK_WIN:
                return myColor == ChessColor.BLACK ? "你赢了" : "你输了";
            default:
                return result.name();
        }
    }

    public static String combineResultAndReason(ChessGameResult result, ChessTerminationReason reason, ChessColor myColor) {
        String resultText = formatResult(result, myColor);
        String reasonText = formatTerminationReason(reason);
        if (TextUtils.isEmpty(reasonText)) {
            return resultText;
        }
        return resultText + " · " + reasonText;
    }
}
