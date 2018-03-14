package com.example.shengshuqiang.morse.widgets;

import android.app.Service;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.popupwindow.MongoliaPopupWindow;
import com.example.shengshuqiang.morse.data.MorseMessageIntermediateItemData;

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
    // 是否为有效关闭，区别返回键
    private boolean isValidClose = false;


    public MorseItemDetailInfoPopupWindow(final Context context) {
        super(context);

        morseItemDetailInfoView = new MorseItemDetailInfoView(context);
        morseItemDetailInfoView.setOnDetailInfoClick(new MorseItemDetailInfoView.OnDetailInfoClick() {
            @Override
            public void onEditClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data) {

            }

            @Override
            public void onDelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data) {
                isValidClose = true;
                dismiss();
            }

            @Override
            public void onOKClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data) {
                isValidClose = true;
                dismiss();
            }

            @Override
            public void onCancelClick(MorseItemDetailInfoView itemDetailInfoView, MorseMessageIntermediateItemData data) {
                isValidClose = true;
                dismiss();
            }
        });
        setContentView(morseItemDetailInfoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void show(View view) {
        super.show(view);

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 300);
    }

    public void setData(MorseMessageIntermediateItemData intermediateItemData) {
        morseItemDetailInfoView.setData(intermediateItemData);
    }

    public MorseMessageIntermediateItemData getIntermediateItemData() {
        return morseItemDetailInfoView.getIntermediateItemData();
    }

    public boolean isValidClose() {
        return isValidClose;
    }
}
