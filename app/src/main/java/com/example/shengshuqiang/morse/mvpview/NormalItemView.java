package com.example.shengshuqiang.morse.mvpview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.shengshuqiang.morse.R;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class NormalItemView extends LinearLayout {

    private TextView titleTxtView;
    private TextView descTxtView;
    private View operateView;
    private View editView;
    private View deleteView;

    private INormalItemViewData normalItemViewData;
    private OnNormalItemViewClickListener onNormalItemViewClickListener;
    private float eventX;
    private float eventY;
    private Scroller scroller;
    // 操作view是否打开（左滑展开）
    private boolean isOperateOpen = false;

    public NormalItemView(Context context) {
        this(context, null);
    }

    public NormalItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        scroller = new Scroller(context);

        setPadding(50, 15, 50, 15);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        inflate(context, R.layout.normal_item_view, this);

        titleTxtView = (TextView) findViewById(R.id.title);
        descTxtView = (TextView) findViewById(R.id.desc);

        operateView = findViewById(R.id.operate);
        editView = findViewById(R.id.edit);
        deleteView = findViewById(R.id.delete);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNormalItemViewClickListener != null) {
                    onNormalItemViewClickListener.onClick(NormalItemView.this, normalItemViewData);
                }
            }
        });

        editView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNormalItemViewClickListener != null) {
                    onNormalItemViewClickListener.onEdit(NormalItemView.this, normalItemViewData);
                }
            }
        });

        deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNormalItemViewClickListener != null) {
                    onNormalItemViewClickListener.onDelete(NormalItemView.this, normalItemViewData);
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        operateView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST), heightMeasureSpec);

        Log.e("SSU", "NormalItemView#onMeasure: widthMeasureSpec=" + MeasureSpec.toString(widthMeasureSpec) + ", heightMeasureSpec= " + MeasureSpec.toString(heightMeasureSpec)
        + ", width=" + getMeasuredWidth() + ", height=" + getMeasuredHeight() + ", operateViewWidth=" + operateView.getMeasuredWidth());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        float deltaX = eventX - x;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                eventX = x;
                eventY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaY = eventY - y;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    int scrollToX;
                    int operateViewWidth = operateView.getWidth();
                    if (!isOperateOpen && deltaX >= 0) {
                        // 左移，最大距离为operateView宽度，最小为0
                        scrollToX = (int) Math.max(0, Math.min(deltaX, operateViewWidth));
                        scrollTo(scrollToX, scrollY);
                        getParent().requestDisallowInterceptTouchEvent(true);

                        Log.e("SSU", "NormalItemView#onTouchEvent move left:deltaX=" + deltaX + ", deltaY=" + deltaY +", scrollX=" + scrollX + ", scrollTo(" + scrollToX + "," + scrollY + ")");
                    } else if (isOperateOpen && deltaX < 0) {
                        // 右移，最大距离为operateView宽度，最小为0
                        scrollToX = (int) Math.max(0, Math.min(operateViewWidth + deltaX, operateViewWidth));
                        scrollTo(scrollToX, scrollY);
                        getParent().requestDisallowInterceptTouchEvent(true);

                        Log.e("SSU", "NormalItemView#onTouchEvent move right:deltaX=" + deltaX + ", deltaY=" + deltaY +", scrollX=" + scrollX + ", scrollTo(" + scrollToX + "," + scrollY + ")");
                    }
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                break;

            case MotionEvent.ACTION_UP:
                scrollEnd(deltaX);
                break;

            case MotionEvent.ACTION_CANCEL:
                scrollEnd(deltaX);
                break;

            default:
                scrollEnd(deltaX);
                break;
        }

        Log.e("SSU", "NormalItemView#onTouchEvent:ScrollX=" + getScrollX() + ", ScrollY=" + scrollY
                + ", event=" + MotionEvent.actionToString(action) + ", x=" + x + ", y=" + y);

        super.onTouchEvent(event);

        return true;
    }

    private void scrollEnd(float deltaX) {
        eventX = 0;
        eventY = 0;

        getParent().requestDisallowInterceptTouchEvent(false);

        int operateViewWidth = operateView.getWidth();
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int dx;
        int dy = 0;
        if (deltaX >= 0) {
            // 左移
            if (scrollX >= operateViewWidth / 2) {
                dx = operateViewWidth - scrollX;
                isOperateOpen = true;
            } else {
                dx = 0 - scrollX;
            }
        } else {
            // 右移
            if (scrollX <= operateViewWidth / 2) {
                dx = 0 - scrollX;
                isOperateOpen = false;
            } else {
                dx = operateViewWidth - scrollX;
            }
        }
        scroller.startScroll(scrollX, scrollY, dx, dy, 300);
        postInvalidate();

        Log.e("SSU", "NormalItemView#scrollEnd:scrollX=" + scrollX + ", scrollY=" + scrollY
                + ", dx=" + dx + ", dy=" + dy + ", width=" + getWidth());

    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), getScrollY());
            postInvalidate();
        }
    }

    public void setOnNormalItemViewClickListener(OnNormalItemViewClickListener onNormalItemViewClickListener) {
        this.onNormalItemViewClickListener = onNormalItemViewClickListener;
    }

    public void setData(INormalItemViewData normalItemViewData) {
        if (normalItemViewData == this.normalItemViewData) {
            return;
        }

        if (normalItemViewData != null) {
            titleTxtView.setText(normalItemViewData.getTitle());
            descTxtView.setText(normalItemViewData.getDesc());

            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        this.normalItemViewData = normalItemViewData;
    }

    public interface INormalItemViewData {
        String getTitle();

        String getDesc();
    }

    public interface OnNormalItemViewClickListener {
        void onClick(NormalItemView normalItemView, INormalItemViewData normalItemViewData);

        void onEdit(NormalItemView normalItemView, INormalItemViewData normalItemViewData);

        void onDelete(NormalItemView normalItemView, INormalItemViewData normalItemViewData);
    }
}
