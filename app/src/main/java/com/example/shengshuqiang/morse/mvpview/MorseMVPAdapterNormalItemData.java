package com.example.shengshuqiang.morse.mvpview;

import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPAdapterNormalItemData implements MorseMVPAdapter.IMorseMVPAdapterItemData, NormalItemView.INormalItemViewData {
    private MorseMessageItemData data;

    public MorseMVPAdapterNormalItemData(MorseMessageItemData data) {
        this.data = data;
    }

    @Override
    public String getTitle() {
        return data.key;
    }

    @Override
    public String getDesc() {
        return data.value;
    }

    @Override
    public int getViewType() {
        return MorseMVPAdapter.NORMAL_ITEM_VIEW_TYPE;
    }
}
