package com.example.shengshuqiang.morse.mvpview;

import android.view.View;
import android.view.ViewGroup;

import com.example.shengshuqiang.morse.utils.Adapter;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class MorseMVPAdapter extends Adapter<MorseMVPAdapter.IMorseMVPAdapterItemData> {

    public static final int NORMAL_ITEM_VIEW_TYPE = 0;

    public static final int VIEW_TYPE_COUNT = 1;

    private NormalItemView.OnNormalItemViewClickListener onNormalItemViewClickListener;

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        IMorseMVPAdapterItemData item = getItem(position);
        if (item != null) {
            return item.getViewType();
        }

        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IMorseMVPAdapterItemData item = getItem(position);

        switch (getItemViewType(position)) {
            case NORMAL_ITEM_VIEW_TYPE:
                MorseMVPAdapterNormalItemData normalItemData = (MorseMVPAdapterNormalItemData) item;

                NormalItemView normalItemView;

                if (convertView == null) {
                    normalItemView = new NormalItemView(parent.getContext());
                    normalItemView.setOnNormalItemViewClickListener(new NormalItemView.OnNormalItemViewClickListener() {
                        @Override
                        public void onClick(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                            if (onNormalItemViewClickListener != null) {
                                onNormalItemViewClickListener.onClick(normalItemView, normalItemViewData);
                            }
                        }

                        @Override
                        public void onEdit(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                            if (onNormalItemViewClickListener != null) {
                                onNormalItemViewClickListener.onEdit(normalItemView, normalItemViewData);
                            }
                        }

                        @Override
                        public void onDelete(NormalItemView normalItemView, MorseMVPAdapterNormalItemData normalItemViewData) {
                            if (onNormalItemViewClickListener != null) {
                                onNormalItemViewClickListener.onDelete(normalItemView, normalItemViewData);
                            }
                        }
                    });
                } else {
                    normalItemView = (NormalItemView) convertView;
                }

                normalItemView.setData(normalItemData);

                return normalItemView;
        }

        return super.getView(position, convertView, parent);
    }

    public void setOnNormalItemViewClickListener(NormalItemView.OnNormalItemViewClickListener onNormalItemViewClickListener) {
        this.onNormalItemViewClickListener = onNormalItemViewClickListener;
    }

    public interface IMorseMVPAdapterItemData {

        int getViewType();

    }
}
