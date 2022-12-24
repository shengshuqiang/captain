package com.shuqiang.captain.qr.mvpview;

import com.shuqiang.captain.qr.mvpmodule.MorseMessageItemData;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPAdapterNormalItemData implements MorseMVPAdapter.IMorseMVPAdapterItemData {
    public MorseMessageItemData data;

    public MorseMVPAdapterNormalItemData(MorseMessageItemData data) {
        this.data = data;
    }

    public String getTitle() {
        return data.userName;
    }

    public String getPassword() {
        return data.password;
    }

    public String getRemarks() {
        return data.remarks;
    }

    @Override
    public int getViewType() {
        return MorseMVPAdapter.NORMAL_ITEM_VIEW_TYPE;
    }
}
