package com.shuqiang.captain.mvpmodule;

import com.shuqiang.captain.mvp.IMVPContract;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPRequest implements IMVPContract.IMVPLoadRequest<MorseMessageData> {

    private IMorseMessageRequest morseMessageRequest;

    public MorseMVPRequest(IMorseMessageRequest morseMessageRequest) {
        this.morseMessageRequest = morseMessageRequest;
    }

    @Override
    public int getID() {
        return MorseMVPModule.MORSE_MESSAGE_DATA_REQUEST_ID;
    }

    @Override
    public MorseMessageData getData() throws Exception {
        return morseMessageRequest.getData();
    }
}
