package com.example.shengshuqiang.morse.mvpmodule;

import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapter;
import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapterNormalItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMessageData {
    private List<MorseMessageItemData> list;

    public MorseMessageData(List<MorseMessageItemData> list) {
        this.list = list;
    }

    public List<MorseMVPAdapter.IMorseMVPAdapterItemData> getItemList() {
        List<MorseMVPAdapter.IMorseMVPAdapterItemData> itemDataList = null;

        if (list != null) {
            itemDataList = new ArrayList<>();
            for (MorseMessageItemData messageItemData : list) {
                itemDataList.add(new MorseMVPAdapterNormalItemData(messageItemData));
            }
        }

        return itemDataList;
    }
}
