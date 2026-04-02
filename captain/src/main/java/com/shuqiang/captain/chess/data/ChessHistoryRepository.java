package com.shuqiang.captain.chess.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.shuqiang.captain.chess.model.ChessAvatarType;
import com.shuqiang.captain.chess.model.ChessColor;
import com.shuqiang.captain.chess.model.ChessGameRecord;
import com.shuqiang.captain.chess.model.ChessGameResult;
import com.shuqiang.captain.chess.model.ChessTerminationReason;

import java.util.ArrayList;
import java.util.List;

public class ChessHistoryRepository {
    private final ChessHistoryDbHelper dbHelper;

    public ChessHistoryRepository(Context context) {
        dbHelper = new ChessHistoryDbHelper(context.getApplicationContext());
    }

    public void saveGame(ChessGameRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ChessHistoryDbHelper.COLUMN_GAME_ID, record.getGameId());
        values.put(ChessHistoryDbHelper.COLUMN_STARTED_AT, record.getStartedAt());
        values.put(ChessHistoryDbHelper.COLUMN_ENDED_AT, record.getEndedAt());
        values.put(ChessHistoryDbHelper.COLUMN_MY_USERNAME, record.getMyUsername());
        values.put(ChessHistoryDbHelper.COLUMN_MY_AVATAR_TYPE, record.getMyAvatarType().name());
        values.put(ChessHistoryDbHelper.COLUMN_MY_AVATAR_VALUE, record.getMyAvatarValue());
        values.put(ChessHistoryDbHelper.COLUMN_OPPONENT_USERNAME, record.getOpponentUsername());
        values.put(ChessHistoryDbHelper.COLUMN_OPPONENT_AVATAR_TYPE, record.getOpponentAvatarType().name());
        values.put(ChessHistoryDbHelper.COLUMN_OPPONENT_AVATAR_VALUE, record.getOpponentAvatarValue());
        values.put(ChessHistoryDbHelper.COLUMN_MY_COLOR, record.getMyColor().name());
        values.put(ChessHistoryDbHelper.COLUMN_RESULT, record.getResult().name());
        values.put(ChessHistoryDbHelper.COLUMN_TERMINATION_REASON, record.getTerminationReason().name());
        values.put(ChessHistoryDbHelper.COLUMN_MOVE_COUNT, record.getMoveCount());
        values.put(ChessHistoryDbHelper.COLUMN_FINAL_FEN, record.getFinalFen());
        values.put(ChessHistoryDbHelper.COLUMN_PGN_TEXT, record.getPgnText());
        values.put(ChessHistoryDbHelper.COLUMN_ABORTED_FLAG, record.isAborted() ? 1 : 0);
        db.insertWithOnConflict(ChessHistoryDbHelper.TABLE_GAMES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ChessGameRecord> listGames() {
        List<ChessGameRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ChessHistoryDbHelper.TABLE_GAMES, null, null, null, null, null,
                ChessHistoryDbHelper.COLUMN_ENDED_AT + " DESC");
        try {
            while (cursor.moveToNext()) {
                records.add(readRecord(cursor));
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    public ChessGameRecord findById(String gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ChessHistoryDbHelper.TABLE_GAMES, null,
                ChessHistoryDbHelper.COLUMN_GAME_ID + "=?", new String[]{gameId},
                null, null, null, "1");
        try {
            if (cursor.moveToFirst()) {
                return readRecord(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private ChessGameRecord readRecord(Cursor cursor) {
        ChessGameRecord record = new ChessGameRecord();
        record.setGameId(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_GAME_ID)));
        record.setStartedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_STARTED_AT)));
        record.setEndedAt(cursor.getLong(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_ENDED_AT)));
        record.setMyUsername(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_MY_USERNAME)));
        record.setMyAvatarType(ChessAvatarType.fromValue(
                cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_MY_AVATAR_TYPE))));
        record.setMyAvatarValue(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_MY_AVATAR_VALUE)));
        record.setOpponentUsername(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_OPPONENT_USERNAME)));
        record.setOpponentAvatarType(ChessAvatarType.fromValue(
                cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_OPPONENT_AVATAR_TYPE))));
        record.setOpponentAvatarValue(
                cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_OPPONENT_AVATAR_VALUE)));
        record.setMyColor(ChessColor.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_MY_COLOR))));
        record.setResult(ChessGameResult.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_RESULT))));
        record.setTerminationReason(ChessTerminationReason.valueOf(
                cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_TERMINATION_REASON))));
        record.setMoveCount(cursor.getInt(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_MOVE_COUNT)));
        record.setFinalFen(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_FINAL_FEN)));
        record.setPgnText(cursor.getString(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_PGN_TEXT)));
        record.setAborted(cursor.getInt(cursor.getColumnIndexOrThrow(ChessHistoryDbHelper.COLUMN_ABORTED_FLAG)) == 1);
        return record;
    }
}
