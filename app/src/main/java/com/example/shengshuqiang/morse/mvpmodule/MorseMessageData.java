package com.example.shengshuqiang.morse.mvpmodule;

import android.text.TextUtils;

import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapter;
import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapterNormalItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMessageData {
    public String title;
    private List<MorseMessageItemData> itemDataList = new ArrayList<>();
    ;

    public List<MorseMVPAdapter.IMorseMVPAdapterItemData> getItemList() {
        List<MorseMVPAdapter.IMorseMVPAdapterItemData> itemDataList = new ArrayList<>();
        for (MorseMessageItemData messageItemData : this.itemDataList) {
            itemDataList.add(new MorseMVPAdapterNormalItemData(messageItemData));
        }

        return itemDataList;
    }

    public void changeItemData(MorseMessageItemData itemData) {
        if (itemData != null) {
            for (MorseMessageItemData messageItemData : itemDataList) {
                if (TextUtils.equals(itemData.userName, messageItemData.userName)) {
                    messageItemData.password = itemData.password;
                    messageItemData.remarks = itemData.remarks;
                }
            }
        }
    }

    public void removeItemData(MorseMessageItemData itemData) {
        if (itemData != null) {
            MorseMessageItemData targetData = contains(itemData.userName);

            if (targetData != null) {
                itemDataList.remove(targetData);
            }
        }
    }

    public void addItemData(MorseMessageItemData itemData) {
        if (itemData != null) {
            itemDataList.add(itemData);
        }
    }

    public MorseMessageItemData contains(String userName) {
        MorseMessageItemData itemData = null;

        for (MorseMessageItemData messageItemData : itemDataList) {
            if (TextUtils.equals(userName, messageItemData.userName)) {
                itemData = messageItemData;
                break;
            }
        }

        return itemData;
    }
}
