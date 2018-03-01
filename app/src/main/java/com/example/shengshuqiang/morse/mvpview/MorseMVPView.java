package com.example.shengshuqiang.morse.mvpview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.example.shengshuqiang.morse.mvp.IMVPContract;
import com.example.shengshuqiang.morse.mvp.MVPView;
import com.example.shengshuqiang.morse.mvppresenter.MorseMVPPresenter;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class MorseMVPView extends MVPView {

    public static final int LIST_VIEW_ID = 0;

    private ListView listView;
    private MorseMVPAdapter adapter;

    public MorseMVPView(Context context) {
        this(context, null);
    }

    public MorseMVPView(Context context, AttributeSet attrs) {
        super(context, attrs);

        listView = new ListView(context);

        addView(listView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        adapter = new MorseMVPAdapter();
        adapter.setOnNormalItemViewClickListener(new NormalItemView.OnNormalItemViewClickListener() {
            @Override
            public void onClick(NormalItemView normalItemView, NormalItemView.INormalItemViewData normalItemViewData) {
                mvpPresenter.doAction(new IMVPContract.IMVPActionData() {
                    @Override
                    public int getID() {
                        return MorseMVPPresenter.CLICK_ITEM_ACTION_ID;
                    }
                });
            }

            @Override
            public void onEdit(NormalItemView normalItemView, NormalItemView.INormalItemViewData normalItemViewData) {
                mvpPresenter.doAction(new IMVPContract.IMVPActionData() {
                    @Override
                    public int getID() {
                        return MorseMVPPresenter.EDIT_ITEM_ACTION_ID;
                    }
                });
            }

            @Override
            public void onDelete(NormalItemView normalItemView, NormalItemView.INormalItemViewData normalItemViewData) {
                mvpPresenter.doAction(new IMVPContract.IMVPActionData() {
                    @Override
                    public int getID() {
                        return MorseMVPPresenter.DELETE_ITEM_ACTION_ID;
                    }
                });
            }
        });
        listView.setAdapter(adapter);
    }

    @Override
    public void show(IMVPContract.IMVPViewData data) {
        switch (data.getID()) {
            case LIST_VIEW_ID:
                MorseMVPListData morseMVPListData = (MorseMVPListData) data;
                adapter.setData(morseMVPListData.list);

                break;
        }
    }

}
