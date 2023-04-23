package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.shuqiang.captain.qr.QRActivity;
import java.util.ArrayList;
import java.util.List;
import captain.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 添加默认的返回图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
        // 设置返回键可用
        getSupportActionBar().setHomeButtonEnabled(true);

        ((GridView) findViewById(R.id.gridview)).setAdapter(new GridAdapter(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            // 返回键
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class GridAdapter extends BaseAdapter {
        private Context context;

        private List<Item> list = new ArrayList<>();

        public GridAdapter(Context mContext) {
            super();
            this.context = mContext;

            list.add(new Item("密码二维马", R.drawable.zxing, QRActivity.class));

            // 一个的话直接跳
            if (list.size() == 1) {
                Item item = list.get(0);
                Intent intent = new Intent(context, item.clzss);
                context.startActivity(intent);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.grid_item, parent, false);
                holder.titleTxtView = (TextView) convertView.findViewById(R.id.title);
                holder.titleTxtView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                holder.iconImgView = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Item item = list.get(position);
            holder.titleTxtView.setText(item.title);
            holder.iconImgView.setImageResource(item.iconRes);
            holder.iconImgView.setOnTouchListener(onTouchListener);
            holder.iconImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, item.clzss);
                    context.startActivity(intent);
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView titleTxtView;
            ImageView iconImgView;
        }

        class Item {
            String title;
            int iconRes;
            Class clzss;

            public Item(String title, int iconRes, Class clzss) {
                this.title = title;
                this.iconRes = iconRes;
                this.clzss = clzss;
            }
        }

        public View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        changeLight((ImageView) view, 0);
//                    listener.onCustomItemClk(listener.getPostion());
//                    view.getParent().requestDisallowInterceptTouchEvent(false);//通知父控件勿拦截本控件
                        // onclick
                        break;
                    case MotionEvent.ACTION_DOWN:
                        changeLight((ImageView) view, -80);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // changeLight(view, 0);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        changeLight((ImageView) view, 0);
                        break;
                    default:
                        changeLight((ImageView) view, 0);
                        break;
                }
                return false;
            }

        };

        private void changeLight(ImageView imageview, int brightness) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0,
                    brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
            imageview.setColorFilter(new ColorMatrixColorFilter(matrix));
        }
    }
}