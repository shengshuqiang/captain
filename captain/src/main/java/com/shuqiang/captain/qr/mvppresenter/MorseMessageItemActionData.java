package com.shuqiang.captain.qr.mvppresenter;

import com.shuqiang.captain.qr.mvp.IMVPContract;
import com.shuqiang.captain.qr.mvpmodule.MorseMessageItemData;

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
