package com.shuqiang.captain.chess.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import captain.R;

public class ChessBoardView extends View {
    public interface OnSquareTapListener {
        void onSquareTap(String square);
    }

    private final Paint lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint darkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lastMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legalMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint coordinatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint whitePiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blackPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final String[][] boardCells = new String[8][8];
    private final Set<String> legalTargets = new HashSet<>();
    private OnSquareTapListener onSquareTapListener;
    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private String selectedSquare;
    private String lastMoveUci;
    private boolean flipped;

    public ChessBoardView(Context context) {
        super(context);
        init();
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lightPaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_light));
        darkPaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_dark));
        selectedPaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_selected));
        lastMovePaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_last_move));
        legalMovePaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_legal_move));
        coordinatePaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_coordinate));
        coordinatePaint.setTextAlign(Paint.Align.CENTER);
        coordinatePaint.setTextSize(dp(10));
        whitePiecePaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_white_piece));
        whitePiecePaint.setTextAlign(Paint.Align.CENTER);
        whitePiecePaint.setShadowLayer(dp(1.5f), 0f, dp(1f),
                ContextCompat.getColor(getContext(), R.color.chess_board_piece_shadow));
        blackPiecePaint.setColor(ContextCompat.getColor(getContext(), R.color.chess_board_black_piece));
        blackPiecePaint.setTextAlign(Paint.Align.CENTER);
        parseFen();
    }

    public void setFen(String fen) {
        if (TextUtils.isEmpty(fen)) {
            return;
        }
        this.fen = fen;
        parseFen();
        invalidate();
    }

    public void setSelectedSquare(String selectedSquare) {
        this.selectedSquare = selectedSquare;
        invalidate();
    }

    public void setLastMoveUci(String lastMoveUci) {
        this.lastMoveUci = lastMoveUci;
        invalidate();
    }

    public void setLegalTargets(List<String> targets) {
        legalTargets.clear();
        if (targets != null) {
            legalTargets.addAll(targets);
        }
        invalidate();
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        invalidate();
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setOnSquareTapListener(OnSquareTapListener onSquareTapListener) {
        this.onSquareTapListener = onSquareTapListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = (int) dp(320);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int resolvedWidth = resolveSize(desiredSize, widthMeasureSpec);
        int resolvedHeight = resolveSize(desiredSize, heightMeasureSpec);
        int size = widthMode == MeasureSpec.UNSPECIFIED ? desiredSize : resolvedWidth;
        if (heightMode != MeasureSpec.UNSPECIFIED) {
            size = Math.min(size, resolvedHeight);
        }
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float boardSize = Math.min(getWidth(), getHeight());
        float squareSize = boardSize / 8f;
        whitePiecePaint.setTextSize(squareSize * 0.68f);
        blackPiecePaint.setTextSize(squareSize * 0.68f);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                rect.set(column * squareSize, row * squareSize, (column + 1) * squareSize, (row + 1) * squareSize);
                canvas.drawRect(rect, ((row + column) % 2 == 0) ? lightPaint : darkPaint);
                String square = getSquareName(row, column);
                if (isLastMoveSquare(square)) {
                    canvas.drawRect(rect, lastMovePaint);
                }
                if (square.equals(selectedSquare)) {
                    canvas.drawRect(rect, selectedPaint);
                }
                if (legalTargets.contains(square)) {
                    canvas.drawCircle(rect.centerX(), rect.centerY(), squareSize * 0.14f, legalMovePaint);
                }
                drawPiece(canvas, row, column, rect.centerX(), rect.centerY(), squareSize);
                drawCoordinates(canvas, row, column, squareSize);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            float squareSize = getWidth() / 8f;
            int column = (int) (event.getX() / squareSize);
            int row = (int) (event.getY() / squareSize);
            if (row >= 0 && row < 8 && column >= 0 && column < 8 && onSquareTapListener != null) {
                onSquareTapListener.onSquareTap(getSquareName(row, column));
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void drawPiece(Canvas canvas, int row, int column, float centerX, float centerY, float squareSize) {
        String piece = boardCells[row][column];
        if (TextUtils.isEmpty(piece)) {
            return;
        }
        String glyph = glyphForPiece(piece.charAt(0));
        if (TextUtils.isEmpty(glyph)) {
            return;
        }
        Paint paint = Character.isUpperCase(piece.charAt(0)) ? whitePiecePaint : blackPiecePaint;
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textBaseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f;
        canvas.drawText(glyph, centerX, textBaseline, paint);
    }

    private void drawCoordinates(Canvas canvas, int row, int column, float squareSize) {
        String square = getSquareName(row, column);
        if (row == 7) {
            canvas.drawText(String.valueOf(square.charAt(0)), column * squareSize + squareSize - dp(8),
                    getHeight() - dp(6), coordinatePaint);
        }
        if (column == 0) {
            canvas.drawText(String.valueOf(square.charAt(1)), dp(8),
                    row * squareSize + dp(12), coordinatePaint);
        }
    }

    private boolean isLastMoveSquare(String square) {
        return !TextUtils.isEmpty(lastMoveUci)
                && lastMoveUci.length() >= 4
                && (square.equals(lastMoveUci.substring(0, 2)) || square.equals(lastMoveUci.substring(2, 4)));
    }

    private String glyphForPiece(char pieceChar) {
        switch (pieceChar) {
            case 'K':
                return "♔";
            case 'Q':
                return "♕";
            case 'R':
                return "♖";
            case 'B':
                return "♗";
            case 'N':
                return "♘";
            case 'P':
                return "♙";
            case 'k':
                return "♚";
            case 'q':
                return "♛";
            case 'r':
                return "♜";
            case 'b':
                return "♝";
            case 'n':
                return "♞";
            case 'p':
                return "♟";
            default:
                return "";
        }
    }

    private void parseFen() {
        for (String[] row : boardCells) {
            Arrays.fill(row, null);
        }
        String[] parts = fen.split(" ");
        if (parts.length == 0) {
            return;
        }
        String[] ranks = parts[0].split("/");
        for (int row = 0; row < ranks.length && row < 8; row++) {
            int column = 0;
            for (char cell : ranks[row].toCharArray()) {
                if (Character.isDigit(cell)) {
                    column += Character.digit(cell, 10);
                } else if (column < 8) {
                    boardCells[row][column] = String.valueOf(cell);
                    column++;
                }
            }
        }
    }

    private String getSquareName(int row, int column) {
        int fileIndex = flipped ? 7 - column : column;
        int rankIndex = flipped ? row + 1 : 8 - row;
        char file = (char) ('a' + fileIndex);
        return String.valueOf(file) + rankIndex;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
