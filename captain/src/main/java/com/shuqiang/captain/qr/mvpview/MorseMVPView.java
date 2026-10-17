package com.shuqiang.captain.qr.mvpview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import captain.R;
import com.shuqiang.captain.qr.mvp.IMVPContract;
import com.shuqiang.captain.qr.mvp.MVPView;
import com.shuqiang.captain.qr.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.qr.mvppresenter.MorseMessageItemActionData;

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

        listView = new ListView(context) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                Log.e("SSU", "dispatchTouchEvent: event=" + MotionEvent.actionToString(ev.getAction()));

                boolean result = super.dispatchTouchEvent(ev);

                Log.e("SSU", "dispatchTouchEvent: result=" + result);
                return result;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                Log.e("SSU", "onInterceptTouchEvent: event=" + MotionEvent.actionToString(ev.getAction()));

                boolean result = super.onInterceptTouchEvent(ev);

                Log.e("SSU", "onInterceptTouchEvent: result=" + result);
                return result;
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                Log.e("SSU", "onTouchEvent: event=" + MotionEvent.actionToString(ev.getAction()));

                boolean result = super.onTouchEvent(ev);

                Log.e("SSU", "onTouchEvent: result=" + result);

                return result;
            }
        };

        addView(listView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        adapter = new MorseMVPAdapter();
        adapter.setOnNormalItemViewClickListener(new NormalItemView.OnNormalItemViewClickListener() {
            @Override
            public void onClick(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.CLICK_ITEM_ACTION_ID, normalItemViewData.data));
            }

            @Override
            public void onEdit(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.MODIFY_ITEM_ACTION_ID, normalItemViewData.data));
            }

            @Override
            public void onDelete(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.DELETE_ITEM_ACTION_ID, normalItemViewData.data));
            }
        });
        listView.setAdapter(adapter);
        TextView descTxtView = new TextView(context);
        // 空态使用同一套逻辑间距与文字尺寸，系统字体缩放由 sp 资源处理。
        int pageSpace = getResources().getDimensionPixelSize(R.dimen.captain_space_page_horizontal);
        descTxtView.setPadding(pageSpace, pageSpace, pageSpace, pageSpace);
        descTxtView.setLineSpacing(0, 1.5f);
        descTxtView.setTextColor(ContextCompat.getColor(context, R.color.captain_text_secondary));
        descTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.captain_text_body));
        descTxtView.setText("1. 点击中间\"新增\"按钮添加账号密码信息。" +
                "\n2. 点击右侧\"保存\"按钮输入加密密码和同样二次确认加密密码生成账号密码信息二维码图片。" +
                "\n3. 点击左侧\"扫码\"按钮扫码或者打开相册选择账号密码信息二维码图片，输入加密密码即可显示。");
        addView(descTxtView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        listView.setEmptyView(descTxtView);
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
