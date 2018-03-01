package com.example.shengshuqiang.morse.widgets;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.shengshuqiang.morse.R;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;

/**
 * Created by shengshuqiang on 2018/2/20.
 */
public class MorseItemDetailInfoView extends LinearLayout {
    private View editView;
    private View deleteView;
    private View okView;
    private View cancelView;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText descEditText;

    private OnDetailInfoClick onDetailInfoClick;
    private MorseMessageItemData data;

    public MorseItemDetailInfoView(Context context) {
        super(context);

        init(context);
    }

    public MorseItemDetailInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);
        setClickable(true);
        inflate(context, R.layout.normal_item_detail_info_view, this);

        editView = findViewById(R.id.edit);
        deleteView = findViewById(R.id.delete);
        okView = findViewById(R.id.ok);
        cancelView = findViewById(R.id.cancel);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        passwordEditText = (EditText) findViewById(R.id.password);
        descEditText = (EditText) findViewById(R.id.desc);

        editView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onEditClick(MorseItemDetailInfoView.this, data);
                }
            }
        });

        deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onDelClick(MorseItemDetailInfoView.this, data);
                }
            }
        });

        okView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onOKClick(MorseItemDetailInfoView.this, data);
                }
            }
        });

        cancelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDetailInfoClick != null) {
                    onDetailInfoClick.onCancelClick(MorseItemDetailInfoView.this, data);
                }
            }
        });
    }

    public void setOnDetailInfoClick(OnDetailInfoClick onDetailInfoClick) {
        this.onDetailInfoClick = onDetailInfoClick;
    }

    @SuppressWarnings("WrongConstant")
    public void setData(MorseMessageItemData data, MorseItemDetailInfoPopupWindow.MODE mode) {
        this.data = data;

        userNameEditText.setText(data != null ? data.userName : null);
        userNameEditText.setEnabled(mode.isUserNameEditable());

        passwordEditText.setText(data != null ? data.password : null);
        passwordEditText.setEnabled(mode.isPasswordEditable());

        descEditText.setText(data != null ? data.desc : null);
        descEditText.setEnabled(mode.isDescEditable());

        editView.setVisibility(mode.getEditOperateVisibility());
        deleteView.setVisibility(mode.getDelOperateVisibility());
        okView.setVisibility(mode.getOkOperateVisibility());
        cancelView.setVisibility(mode.getCancelOperateVisibility());
    }

    public MorseMessageItemData getOldData() {
        return data;
    }

    public MorseMessageItemData getNewData() {
        String userName = String.valueOf(userNameEditText.getText());
        String password = String.valueOf(passwordEditText.getText());
        String desc = String.valueOf(descEditText.getText());
        MorseMessageItemData messageItemData = new MorseMessageItemData(userName, password, desc);

        return messageItemData;
    }

    public interface OnDetailInfoClick {
        void onEditClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data);
        void onDelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data);
        void onOKClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data);
        void onCancelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data);
    }
}
