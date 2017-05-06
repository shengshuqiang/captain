package com.example.shengshuqiang.morse.mvppresenter;

import android.content.Context;

import com.example.shengshuqiang.morse.mvp.IMVPContract;
import com.example.shengshuqiang.morse.mvp.MVPPresenter;
import com.example.shengshuqiang.morse.mvpmodule.MorseMVPLoadRequest;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageData;
import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapter;
import com.example.shengshuqiang.morse.mvpview.MorseMVPListData;

import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class MorseMVPPresenter extends MVPPresenter {

    private Context context;

    public MorseMVPPresenter(Context context) {
        this.context = context;
    }

    public void start() {

        mvpModule.loadData(new MorseMVPLoadRequest(), new IMVPContract.IMVPLoaderCallbacks<MorseMessageData>() {
            @Override
            public void onSuccess(MorseMessageData morseMessageItemData) {
                List<MorseMVPAdapter.IMorseMVPAdapterItemData> list = (null != morseMessageItemData ? morseMessageItemData.getItemList() : null);
                MorseMVPListData data = new MorseMVPListData(list);
                mvpView.show(data);
            }

            @Override
            public void onError(Exception exception) {
                MorseMVPListData data = new MorseMVPListData(null);
                mvpView.show(data);
            }
        });
    }

    @Override
    public void doAction(IMVPContract.IMVPActionData data) {

    }


}
