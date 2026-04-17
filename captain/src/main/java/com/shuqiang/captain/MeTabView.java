package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.Utils;
import com.captain.base.WebViewActivity;
import captain.R;

// 我的 Tab
public class MeTabView extends FrameLayout {
    // 隐私政策列表项
    public static final String PRIVACY_POLICY_ITEM = "隐私政策";
    // 列表项数组
    public static final String[] ITEMS = { PRIVACY_POLICY_ITEM };
    private static final int HIDDEN_FEATURE_TAP_TARGET = 6;
    private static final long HIDDEN_FEATURE_TAP_TIMEOUT_MS = 1200L;
    private int remainingTapCount = HIDDEN_FEATURE_TAP_TARGET;
    private long lastTapTimestamp;

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
        inflate(context, R.layout.me_tab_layout_new, this);
        // 版本号
        TextView versionView = findViewById(R.id.app_version);
        versionView.setText("V" + Utils.getAppVersionName(context));
        versionView.setOnClickListener(v -> onVersionClicked(context));

        RecyclerView recyclerView = findViewById(R.id.list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.me_list_item, parent, false);
                return new ViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
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
                return ITEMS.length;
            }
        });
        // 添加卡片间距装饰器
        int cardGap = context.getResources().getDimensionPixelSize(R.dimen.captain_space_card_gap);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = cardGap;
            }
        });
    }

    private void onVersionClicked(Context context) {
        if (HiddenFeaturePrefs.isHiddenFeaturesEnabled(context)) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastTapTimestamp > HIDDEN_FEATURE_TAP_TIMEOUT_MS) {
            remainingTapCount = HIDDEN_FEATURE_TAP_TARGET;
        }
        lastTapTimestamp = now;
        remainingTapCount--;
        if (remainingTapCount <= 0) {
            if (HiddenFeaturePrefs.enableHiddenFeatures(context)) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).switchToHomeTab();
                }
                Toast.makeText(context, "隐藏功能已开启，已切换到首页展示全部功能", Toast.LENGTH_SHORT).show();
            }
            remainingTapCount = HIDDEN_FEATURE_TAP_TARGET;
            return;
        }
        if (remainingTapCount <= 2) {
            Toast.makeText(context, "再点击" + remainingTapCount + "次开启隐藏功能", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.txtView = itemView.findViewById(R.id.item_text);
        }

        public void setItemName(String name) {
            this.txtView.setText(name);
        }

        public void setOnClickListener(OnClickListener onClickListener) {
            this.itemView.setOnClickListener(onClickListener);
        }
    }
}
