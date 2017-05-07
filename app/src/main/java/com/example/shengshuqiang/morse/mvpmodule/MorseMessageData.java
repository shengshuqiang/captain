package com.example.shengshuqiang.morse.mvpmodule;

import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapter;
import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapterNormalItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMessageData {
    public String title;
    private List<MorseMessageItemData> data;

    public List<MorseMVPAdapter.IMorseMVPAdapterItemData> getItemList() {
        List<MorseMVPAdapter.IMorseMVPAdapterItemData> itemDataList = null;

        if (data != null) {
            itemDataList = new ArrayList<>();
            for (MorseMessageItemData messageItemData : data) {
                itemDataList.add(new MorseMVPAdapterNormalItemData(messageItemData));
            }
        }

        return itemDataList;
    }
}
