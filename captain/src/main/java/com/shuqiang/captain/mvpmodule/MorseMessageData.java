package com.shuqiang.captain.mvpmodule;

import android.text.TextUtils;

import com.shuqiang.captain.mvpview.MorseMVPAdapter;
import com.shuqiang.captain.mvpview.MorseMVPAdapterNormalItemData;
import com.shuqiang.captain.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMessageData {
    public static final String SPLIT_FLAG_CHAR = "\n";

    private List<MorseMessageItemData> itemDataList = new ArrayList<>();

    public List<MorseMVPAdapter.IMorseMVPAdapterItemData> getAdapterItemList() {
        List<MorseMVPAdapter.IMorseMVPAdapterItemData> itemDataList = new ArrayList<>();
        for (MorseMessageItemData messageItemData : this.itemDataList) {
            itemDataList.add(new MorseMVPAdapterNormalItemData(messageItemData));
        }

        return itemDataList;
    }

    public List<MorseMessageItemData> getItemDataList() {
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
            MorseMessageItemData targetData = get(itemData.userName);

            if (targetData != null) {
                itemDataList.remove(targetData);
            }
        }
    }

    public void addItemData(MorseMessageItemData itemData) {
        if (itemData != null) {
            MorseMessageItemData morseMessageItemData = get(itemData.userName);
            if (morseMessageItemData != null) {
                itemDataList.remove(morseMessageItemData);
            }

            itemDataList.add(itemData);
        }
    }

    public MorseMessageItemData get(String userName) {
        MorseMessageItemData itemData = null;

        for (MorseMessageItemData messageItemData : itemDataList) {
            if (TextUtils.equals(userName, messageItemData.userName)) {
                itemData = messageItemData;
                break;
            }
        }

        return itemData;
    }

    public static String cleanInvalidString(String input) {
        String output = null;
        if (!TextUtils.isEmpty(input)) {
            output = input.replace(SPLIT_FLAG_CHAR, "");
        }

        return output;
    }

    public String toSerialize() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!Utils.isEmpty(itemDataList)) {
            int size = itemDataList.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    stringBuilder.append(SPLIT_FLAG_CHAR);
                }

                MorseMessageItemData itemData = itemDataList.get(i);
                stringBuilder.append(itemData.userName).append(SPLIT_FLAG_CHAR)
                        .append(itemData.password).append(SPLIT_FLAG_CHAR)
                        .append(Utils.emptyIfNull(itemData.remarks));
            }
        }

        return stringBuilder.toString();
    }

    public static MorseMessageData toDeserialize(String str) {
        MorseMessageData morseMessageData = null;
        if (!TextUtils.isEmpty(str)) {
            String[] splits = str.split(SPLIT_FLAG_CHAR, Integer.MAX_VALUE);
            if (splits.length % 3 == 0) {
                morseMessageData = new MorseMessageData();
                int itemSize = splits.length / 3;
                List<MorseMessageItemData> itemDataList = morseMessageData.itemDataList;
                for (int i = 0; i < itemSize; i++) {
                    int index = i * 3;
                    // 用户名
                    String userName = splits[index];
                    // 密码
                    String password = splits[index + 1];
                    // 备注
                    String remarks  = splits[index + 2];

                    MorseMessageItemData itemData = new MorseMessageItemData(userName, password, remarks);
                    itemDataList.add(itemData);
                }
            }
        }

        return morseMessageData;
    }
}
