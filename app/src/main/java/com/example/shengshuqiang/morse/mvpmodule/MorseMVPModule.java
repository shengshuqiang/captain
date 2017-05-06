package com.example.shengshuqiang.morse.mvpmodule;

import android.content.Context;

import com.example.shengshuqiang.morse.mvp.IMVPContract;
import com.example.shengshuqiang.morse.mvp.MVPModule;
import com.example.shengshuqiang.morse.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/30.
 */

public class MorseMVPModule extends MVPModule {
    public static final int MORSE_MESSAGE_DATA_REQUEST_ID = 0;

    private Context context;

    public MorseMVPModule(Context context) {
        this.context = context;
    }

    @Override
    protected <DATA> DATA getLoadRequestData(IMVPContract.IMVPLoadRequest<DATA> loadRequest) {
        DATA data = null;
        switch (loadRequest.getID()) {
            case MORSE_MESSAGE_DATA_REQUEST_ID:
                MorseMessageData morseMessageData = null;
                String str = Utils.getFromAssets(context, Utils.MORSE_ASSETS_FILE_NAME);

                try {
                    JSONObject jsonObject = new JSONObject(str);
                    Iterator<String> keys = jsonObject.keys();
                    List<MorseMessageItemData> itemDataList = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = jsonObject.getString(key);

                        itemDataList.add(new MorseMessageItemData(key, value));
                    }

                    morseMessageData = new MorseMessageData(itemDataList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                data = (DATA) morseMessageData;
                break;
        }

        return data;
    }
}
