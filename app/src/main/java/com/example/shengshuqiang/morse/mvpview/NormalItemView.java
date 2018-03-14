package com.example.shengshuqiang.morse.mvpview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
    private TextView passwordTxtView;
    private TextView descTxtView;
    private View operateView;
    private View editView;
    private View deleteView;

    private MorseMVPAdapterNormalItemData normalItemViewData;
    private OnNormalItemViewClickListener onNormalItemViewClickListener;
    private float eventX;
    private float eventY;
    private Scroller scroller;
    // 操作view是否打开（左滑展开）
    private boolean isOperateOpen = false;
    private ViewConfiguration viewConfiguration;

    public NormalItemView(Context context) {
        this(context, null);
    }

    public NormalItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        viewConfiguration = ViewConfiguration.get(context);
        scroller = new Scroller(context);

        setPadding(50, 15, 50, 15);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        inflate(context, R.layout.normal_item_view, this);

        titleTxtView = (TextView) findViewById(R.id.title);
        passwordTxtView = (TextView) findViewById(R.id.password);
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
        float deltaX;
        float deltaY;
        if (eventX > 0 && eventY > 0) {
            deltaX = eventX - x;
            deltaY = eventY - y;
        } else {
            deltaX = 0;
            deltaY = 0;
        }

        int scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        boolean isScrolling = Math.abs(deltaX) > scaledTouchSlop || Math.abs(deltaY) > scaledTouchSlop;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                eventX = x;
                eventY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isScrolling) {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        int scrollToX;
                        int operateViewWidth = operateView.getWidth();
                        if (!isOperateOpen && deltaX >= 0) {
                            // 左移，最大距离为operateView宽度，最小为0
                            scrollToX = (int) Math.max(0, Math.min(deltaX, operateViewWidth));
                            scrollTo(scrollToX, scrollY);
                            getParent().requestDisallowInterceptTouchEvent(true);

                            Log.e("SSU", "NormalItemView#onTouchEvent move left:deltaX=" + deltaX + ", deltaY=" + deltaY + ", scrollX=" + scrollX + ", scrollTo(" + scrollToX + "," + scrollY + ")");
                        } else if (isOperateOpen && deltaX < 0) {
                            // 右移，最大距离为operateView宽度，最小为0
                            scrollToX = (int) Math.max(0, Math.min(operateViewWidth + deltaX, operateViewWidth));
                            scrollTo(scrollToX, scrollY);
                            getParent().requestDisallowInterceptTouchEvent(true);

                            Log.e("SSU", "NormalItemView#onTouchEvent move right:deltaX=" + deltaX + ", deltaY=" + deltaY + ", scrollX=" + scrollX + ", scrollTo(" + scrollToX + "," + scrollY + ")");
                        }
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
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
        if (isScrolling) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        Log.e("SSU", "NormalItemView#onTouchEvent:ScrollX=" + getScrollX() + ", ScrollY=" + scrollY
                + ", event=" + MotionEvent.actionToString(action));

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

    public void setData(MorseMVPAdapterNormalItemData normalItemViewData) {
        if (normalItemViewData == this.normalItemViewData) {
            return;
        }

        if (normalItemViewData != null) {
            titleTxtView.setText(normalItemViewData.getTitle());
            passwordTxtView.setText(normalItemViewData.getPassword());
            descTxtView.setText(normalItemViewData.getRemarks());

            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        scrollTo(0, 0);

        this.normalItemViewData = normalItemViewData;
    }

    public interface OnNormalItemViewClickListener {
        void onClick(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData);

        void onEdit(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData);

        void onDelete(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData);
    }

}
