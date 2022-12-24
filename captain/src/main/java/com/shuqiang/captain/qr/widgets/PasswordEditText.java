package com.shuqiang.captain.qr.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.shuqiang.captain.qr.utils.Utils;

import static android.animation.ValueAnimator.INFINITE;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordEditText extends AppCompatEditText {
    private Paint paint;
    private Paint cursorPaint;
    private Paint backgroundPaint;

    private int drawX;
    private int baseLineY;
    private int drawWidth;
    private int drawSpaceWidth;
    private int drawTxtCount;

    private RectF rect;
    private float round;

    private CharSequence inputCharSeq;
    private int cursorHeight;
    private ValueAnimator cursorValueAnimator = ObjectAnimator.ofFloat(0, 1);

    private IPasswordListener passwordListener;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = buildPaint(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getTextSize());

        drawWidth = (int) (getTextSize() * 2);
        drawSpaceWidth = (int) (getTextSize() / 3);

        cursorPaint = buildPaint(Color.GRAY);
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(Utils.dip2px(context, 2));

        backgroundPaint = buildPaint(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(Utils.dip2px(context, 2));

        rect = new RectF();
        round = Utils.dip2px(context, 5);

        drawTxtCount = 6;

        cursorValueAnimator.setDuration(1500).setRepeatCount(INFINITE);
        cursorValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                cursorPaint.setAlpha((int) ((Float) animation.getAnimatedValue() * 255));
                invalidate();
            }
        });
    }

    public void setPasswordListener(IPasswordListener passwordListener) {
        this.passwordListener = passwordListener;
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
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int width = getMeasuredWidth();
        int height = drawWidth + paddingTop + paddingBottom;

        int drawSumWidth = drawTxtCount * drawWidth + (drawTxtCount - 1) * drawSpaceWidth;
        drawX = paddingLeft + (width - paddingLeft - paddingRight - drawSumWidth) / 2;

        // https://www.cnblogs.com/slgkaifa/p/7101297.html
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

        baseLineY = (int) (height/2 - top/2 - bottom/2);//基线中间点的y轴计算公式

        cursorHeight = drawWidth / 2;

        setMeasuredDimension(getDefaultSize(getMeasuredWidth(), widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        this.setSelection(this.getText().length());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        Editable editable = getText();
        TransformationMethod transformationMethod = getTransformationMethod();
        if (transformationMethod != null) {
            inputCharSeq = transformationMethod.getTransformation(text, this);
        } else {
            inputCharSeq = editable;
        }
        if (cursorValueAnimator != null) {
            if (inputCharSeq.length() < drawTxtCount) {
                cursorValueAnimator.start();
            } else {
                cursorValueAnimator.end();

                if (passwordListener != null) {
                    passwordListener.onComplete(this, text.toString());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        canvas.translate(scrollX, scrollY);

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int height = getHeight();
        for (int i = 0; i < inputCharSeq.length(); i++) {
            int left = drawX + (i * (drawWidth + drawSpaceWidth));
            canvas.drawText(String.valueOf(inputCharSeq.charAt(i)), left + drawWidth / 2, baseLineY, paint);
        }

        int bottom = height - paddingBottom;
        if (inputCharSeq.length() < drawTxtCount) {
            int startX = drawX + (inputCharSeq.length() * (drawWidth + drawSpaceWidth)) + drawWidth / 2;
            int startY = paddingTop + (height - paddingTop - paddingBottom - cursorHeight) / 2;
            int stopX = startX;
            int stopY = startY + cursorHeight;
            canvas.drawLine(startX, startY, stopX, stopY, cursorPaint);
        }

        for (int i = 0; i < drawTxtCount; i++) {
            int left = drawX + (i * (drawWidth + drawSpaceWidth));
            rect.set(left, paddingTop, left + drawWidth, bottom);
            canvas.drawRoundRect(rect, round, round, backgroundPaint);
        }

        canvas.translate(-scrollX, -scrollY);
    }

    public interface IPasswordListener {
        void onComplete(PasswordEditText passwordEditText, String pwd);
    }
}
