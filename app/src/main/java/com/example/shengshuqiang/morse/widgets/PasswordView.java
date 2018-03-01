package com.example.shengshuqiang.morse.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordView extends EditText {
    private Paint paint;
    private Paint backgroundPaint;

    private Rect rect;

    public PasswordView(Context context) {
        this(context, null);
    }

    public PasswordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = buildPaint(Color.BLACK);
        backgroundPaint = buildPaint(Color.GRAY);

        rect = new Rect();
    }

    private Paint buildPaint(int color) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(color);

        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        rect.set(paddingLeft, paddingTop, paddingLeft + getMeasuredWidth(), paddingTop + getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//
//        canvas.drawRect();
    }
}
