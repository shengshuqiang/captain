package com.example.shengshuqiang.morse.mvpview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shengshuqiang.morse.R;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class NormalItemView extends LinearLayout {

    private TextView titleTxtView;
    private TextView descTxtView;

    private INormalItemViewData normalItemViewData;
    private OnNormalItemViewClickListener onNormalItemViewClickListener;

    public NormalItemView(Context context) {
        this(context, null);
    }

    public NormalItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
        setPadding(50, 15, 50, 15);
        inflate(context, R.layout.normal_item_view, this);

        titleTxtView = (TextView) findViewById(R.id.title);
        descTxtView = (TextView) findViewById(R.id.desc);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNormalItemViewClickListener != null) {
                    onNormalItemViewClickListener.onClick(NormalItemView.this, normalItemViewData);
                }
            }
        });
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
    }
}
