package com.example.shengshuqiang.morse.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.popupwindow.MongoliaPopupWindow;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 信息弹窗
 * 1. 单个信息展示
 * 2. 单个信息编辑
 * 3. 单个信息新增
 * <p>
 * Created by shengshuqiang on 2017/5/7.
 */

public class MorseItemDetailInfoPopupWindow extends MongoliaPopupWindow {
    private MorseItemDetailInfoView morseItemDetailInfoView;
    private MorseMessageItemData messageItemData;
    private String userName;
    private MODE mode;
    private boolean isOperateDismiss = false;

    public MorseItemDetailInfoPopupWindow(Context context) {
        super(context);

        morseItemDetailInfoView = new MorseItemDetailInfoView(context);
        morseItemDetailInfoView.setOnDetailInfoClick(new MorseItemDetailInfoView.OnDetailInfoClick() {
            @Override
            public void onEditClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data) {
                setData(data, MODE.WRITE);
            }

            @Override
            public void onDelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data) {
                mode = MODE.DEL;
                messageItemData = morseItemDetailInfoView.getOldData();
                isOperateDismiss = true;
                dismiss();
            }

            @Override
            public void onOKClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data) {
                messageItemData = morseItemDetailInfoView.getNewData();
                isOperateDismiss = true;
                dismiss();
            }

            @Override
            public void onCancelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageItemData data) {
                mode = MODE.READ;
                messageItemData = morseItemDetailInfoView.getOldData();
                isOperateDismiss = true;
                dismiss();
            }
        });
        setContentView(morseItemDetailInfoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void show(View view) {
        super.show(view);
        isOperateDismiss = false;
    }

    public void setData(MorseMessageItemData data, MODE mode) {
        this.mode = mode;
        messageItemData = data;
        morseItemDetailInfoView.setData(data, mode);

        userName = (data != null ? data.userName : null);
    }

    public MorseMessageItemData getMorseMessageItemData() {
        return messageItemData;
    }

    public String getUserName() {
        return userName;
    }

    public MODE getMode() {
        return mode;
    }

    public boolean isOperateDismiss() {
        return isOperateDismiss;
    }
}
