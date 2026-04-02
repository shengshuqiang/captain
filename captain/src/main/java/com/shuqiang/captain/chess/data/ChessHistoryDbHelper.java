package com.shuqiang.captain.chess.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChessHistoryDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "captain_chess.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_GAMES = "games";
    public static final String COLUMN_GAME_ID = "game_id";
    public static final String COLUMN_STARTED_AT = "started_at";
    public static final String COLUMN_ENDED_AT = "ended_at";
    public static final String COLUMN_MY_USERNAME = "my_username";
    public static final String COLUMN_MY_AVATAR_TYPE = "my_avatar_type";
    public static final String COLUMN_MY_AVATAR_VALUE = "my_avatar_value";
    public static final String COLUMN_OPPONENT_USERNAME = "opponent_username";
    public static final String COLUMN_OPPONENT_AVATAR_TYPE = "opponent_avatar_type";
    public static final String COLUMN_OPPONENT_AVATAR_VALUE = "opponent_avatar_value";
    public static final String COLUMN_MY_COLOR = "my_color";
    public static final String COLUMN_RESULT = "result";
    public static final String COLUMN_TERMINATION_REASON = "termination_reason";
    public static final String COLUMN_MOVE_COUNT = "move_count";
    public static final String COLUMN_FINAL_FEN = "final_fen";
    public static final String COLUMN_PGN_TEXT = "pgn_text";
    public static final String COLUMN_ABORTED_FLAG = "aborted_flag";

    public ChessHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_GAMES + " ("
                + COLUMN_GAME_ID + " TEXT PRIMARY KEY,"
                + COLUMN_STARTED_AT + " INTEGER,"
                + COLUMN_ENDED_AT + " INTEGER,"
                + COLUMN_MY_USERNAME + " TEXT,"
                + COLUMN_MY_AVATAR_TYPE + " TEXT,"
                + COLUMN_MY_AVATAR_VALUE + " TEXT,"
                + COLUMN_OPPONENT_USERNAME + " TEXT,"
                + COLUMN_OPPONENT_AVATAR_TYPE + " TEXT,"
                + COLUMN_OPPONENT_AVATAR_VALUE + " TEXT,"
                + COLUMN_MY_COLOR + " TEXT,"
                + COLUMN_RESULT + " TEXT,"
                + COLUMN_TERMINATION_REASON + " TEXT,"
                + COLUMN_MOVE_COUNT + " INTEGER,"
                + COLUMN_FINAL_FEN + " TEXT,"
                + COLUMN_PGN_TEXT + " TEXT,"
                + COLUMN_ABORTED_FLAG + " INTEGER DEFAULT 0"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        onCreate(db);
    }
}
