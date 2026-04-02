package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.captain.base.LDLWebViewActivity;
import com.captain.base.LLDWebViewActivity;
import com.captain.base.WebViewActivity;
import com.shuqiang.captain.chess.ui.ChessLobbyActivity;
import com.shuqiang.captain.qr.QRActivity;
import com.shuqiang.captain.xhs.ui.XhsDownloadActivity;

import java.util.ArrayList;
import java.util.List;

import captain.R;

// 主页面橱窗（九宫格） Tab
public class MainShopTabView extends FrameLayout {

    public MainShopTabView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MainShopTabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainShopTabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MainShopTabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.main_shop_tab_layout_new, this);
        ((GridView) findViewById(R.id.gridview)).setAdapter(new GridAdapter(context));
    }

    private static class GridAdapter extends BaseAdapter {
        private Context context;

        private List<Item> list = new ArrayList<>();

        public GridAdapter(Context mContext) {
            super();
            this.context = mContext;

//            list.add(new Item("信息二维马测试", R.drawable.zxing, QRTestActivity.class));
            Intent arActivityIntent = new Intent(context, QRActivity.class);
            arActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(new Item("信息二维码", R.drawable.zxing, arActivityIntent));
            Intent ldlIntent = new Intent(context, LDLWebViewActivity.class);
            ldlIntent.putExtra(WebViewActivity.URL_KEY, "https://market.m.taobao.com/app/alisports-fe/sports-gym-client/h5/index.html");
            ldlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(new Item("乐动力", R.drawable.ledongli, ldlIntent));
            Intent lldIntent = new Intent(context, LLDWebViewActivity.class);
            lldIntent.putExtra(WebViewActivity.URL_KEY, "https://market.m.taobao.com/app/alisports-fe/sports-gym-client/h5/index.html");
            lldIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(new Item("乐力动", R.drawable.ledongli, lldIntent));
            Intent xhsdActivityIntent = new Intent(context, XhsDownloadActivity.class);
            xhsdActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(new Item("资源嗅探", R.drawable.download, xhsdActivityIntent));
            Intent chessLobbyIntent = new Intent(context, ChessLobbyActivity.class);
            chessLobbyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(new Item("国际象棋", R.drawable.ic_chess_feature, chessLobbyIntent));
//            // 一个的话直接跳
//            if (list.size() == 1) {
//                Item item = list.get(0);
//                Intent intent = new Intent(context, item.clzss);
//                context.startActivity(intent);
//            }
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
                holder.iconImgView = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Item item = list.get(position);
            holder.titleTxtView.setText(item.title);
            holder.iconImgView.setImageResource(item.iconRes);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(item.intent);
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
            Intent intent;

            public Item(String title, int iconRes, Intent intent) {
                this.title = title;
                this.iconRes = iconRes;
                this.intent = intent;
            }
        }

    }
}
