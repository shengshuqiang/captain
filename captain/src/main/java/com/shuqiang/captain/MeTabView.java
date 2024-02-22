package com.shuqiang.captain;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.DividerItemDecoration;
import com.captain.base.Utils;
import com.captain.base.WebViewActivity;
import captain.R;

// 我的 Tab
public class MeTabView extends FrameLayout {
    // 隐私政策列表项
    public static final String PRIVACY_POLICY_ITEM = "隐私政策";
    // 列表项数组
    public static final String[] ITEMS = { PRIVACY_POLICY_ITEM };

    public MeTabView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MeTabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MeTabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MeTabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
//        Log.d("SSU", "init");
        inflate(context, R.layout.me_tab_layout, this);
//        版本号
        ((TextView)findViewById(R.id.app_version)).setText("V" + Utils.getAppVersionName(context));

        RecyclerView recyclerView = findViewById(R.id.list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
                Log.d("SSU", "onCreateViewHolder");
                TextView itemTxtView = new TextView(context);
                int paddingDP = Utils.dip2px(context, 20);
                itemTxtView.setPadding(paddingDP, paddingDP, paddingDP, paddingDP);
                itemTxtView.setLineSpacing(30,1);
                itemTxtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                itemTxtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.right_arrow_forward, 0);
                itemTxtView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, Utils.dip2px(context, 80)));
                itemTxtView.setGravity(Gravity.CENTER_VERTICAL);
                itemTxtView.setBackgroundColor(Color.WHITE);
                return new ViewHolder(itemTxtView);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                Log.d("SSU", "onBindViewHolder" + ITEMS[position] + ", " + position);
                holder.setItemName(ITEMS[position]);
                switch (ITEMS[position]) {
                    case PRIVACY_POLICY_ITEM:
                        holder.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, WebViewActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(WebViewActivity.URL_KEY, v.getResources().getString(R.string.captain_privacy_policy_url));
                                context.startActivity(intent);
                            }
                        });
                        break;
                    default:
                        break;
                }
            }

            @Override
            public int getItemCount() {
                Log.d("SSU", "getItemCount" + ITEMS.length);
                return ITEMS.length;
            }
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtView;
        public ViewHolder(TextView txtView) {
            super(txtView);
            this.txtView = txtView;
        }

        public void setItemName(String name) {
            this.txtView.setText(name);
        }

        public void setOnClickListener(OnClickListener onClickListener) {
            this.txtView.setOnClickListener(onClickListener);
        }
    }
}