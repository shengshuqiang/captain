package com.example.shengshuqiang.morse.mvppresenter;

import android.content.Context;

import com.example.shengshuqiang.morse.mvp.IMVPContract;
import com.example.shengshuqiang.morse.mvp.MVPPresenter;
import com.example.shengshuqiang.morse.mvpmodule.IMorseMessageRequest;
import com.example.shengshuqiang.morse.mvpmodule.MorseMVPRequest;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageData;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;
import com.example.shengshuqiang.morse.mvpview.MorseMVPAdapter;
import com.example.shengshuqiang.morse.mvpview.MorseMVPListData;

import java.util.List;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class MorseMVPPresenter extends MVPPresenter {
    // 点击
    public static final int CLICK_ITEM_ACTION_ID = 0;
    // 编辑
    public static final int EDIT_ITEM_ACTION_ID = 1;
    // 新增
    public static final int ADD_ITEM_ACTION_ID = 2;
    // 删除
    public static final int DELETE_ITEM_ACTION_ID = 3;

    private Context context;
    private IMorseMessageRequest morseMessageRequest;
    private MorseMessageData morseMessageData;

    public MorseMVPPresenter(Context context, IMorseMessageRequest morseMessageRequest) {
        this.context = context;
        this.morseMessageRequest = morseMessageRequest;
    }

    public void start() {

        mvpModule.loadData(new MorseMVPRequest(morseMessageRequest), new IMVPContract.IMVPLoaderCallbacks<MorseMessageData>() {
            @Override
            public void onSuccess(MorseMessageData morseMessageData) {
                List<MorseMVPAdapter.IMorseMVPAdapterItemData> list = (null != morseMessageData ? morseMessageData.getItemList() : null);
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
        int id = data.getID();
        MorseMessageItemData itemData = ((MorseMessageItemActionData) data).getItemData();
        switch (id) {
            case CLICK_ITEM_ACTION_ID:

                break;

            case EDIT_ITEM_ACTION_ID:
                morseMessageData.changeItemData(itemData);
                mvpView.show(new MorseMVPListData(morseMessageData.getItemList()));
                break;

            case ADD_ITEM_ACTION_ID:
                if (morseMessageData == null) {
                    morseMessageData = new MorseMessageData();
                }

                morseMessageData.addItemData(itemData);
                mvpView.show(new MorseMVPListData(morseMessageData.getItemList()));
                break;

            case DELETE_ITEM_ACTION_ID:
                morseMessageData.delItemData(itemData);
                mvpView.show(new MorseMVPListData(morseMessageData.getItemList()));
                break;

            default:
                break;
        }
    }


}
