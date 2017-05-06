package com.example.shengshuqiang.morse.mvpmodule;

import com.example.shengshuqiang.morse.mvp.IMVPContract;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPLoadRequest implements IMVPContract.IMVPLoadRequest<MorseMessageData> {

    @Override
    public int getID() {
        return MorseMVPModule.MORSE_MESSAGE_DATA_REQUEST_ID;
    }
}
