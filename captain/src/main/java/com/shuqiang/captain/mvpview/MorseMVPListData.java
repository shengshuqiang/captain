package com.shuqiang.captain.mvpview;

import com.shuqiang.captain.mvp.IMVPContract;

import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPListData implements IMVPContract.IMVPViewData {

    public List<MorseMVPAdapter.IMorseMVPAdapterItemData> list;

    public MorseMVPListData(List<MorseMVPAdapter.IMorseMVPAdapterItemData> list) {
        this.list = list;
    }

    @Override
    public int getID() {
        return MorseMVPView.LIST_VIEW_ID;
    }
}
