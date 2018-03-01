package com.example.shengshuqiang.morse.mvppresenter;

import com.example.shengshuqiang.morse.mvp.IMVPContract;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;

/**
 * Created by shengshuqiang on 2018/2/21.
 */
public class MorseMessageItemActionData implements IMVPContract.IMVPActionData {
    private int id;
    private MorseMessageItemData itemData;

    public MorseMessageItemActionData(int id, MorseMessageItemData itemData) {
        this.id = id;
        this.itemData = itemData;
    }

    @Override
    public int getID() {
        return id;
    }

    public MorseMessageItemData getItemData() {
        return itemData;
    }
}
