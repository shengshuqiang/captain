package com.shuqiang.captain.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by shengshuqiang on 16/6/20.
 */
public abstract class Adapter<DATA> extends BaseAdapter {
    private List<DATA> list;

    public void setData(List<DATA> list) {
        this.list = list;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (null != list) {
            return list.size();
        }

        return 0;
    }

    @Override
    public DATA getItem(int position) {
        if (isPositionValid(position)) {
            return list.get(position);
        }

        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        throw new IllegalArgumentException("null view in position = " + position + ", item = " + getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    protected boolean isPositionValid(int position) {
        return (position >=0 && position < getCount());
    }
}
