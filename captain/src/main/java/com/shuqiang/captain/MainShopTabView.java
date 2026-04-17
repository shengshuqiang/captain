package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.LDLWebViewActivity;
import com.captain.base.LLDWebViewActivity;
import com.captain.base.WebViewActivity;
import com.shuqiang.captain.chess.ui.ChessLobbyActivity;
import com.shuqiang.captain.qr.QRActivity;
import com.shuqiang.captain.xhs.ui.XhsDownloadActivity;

import java.util.ArrayList;
import java.util.List;

import captain.R;

// 首页功能区升级为桌面式入口面板，支持拖拽换位和本地持久化。
public class MainShopTabView extends FrameLayout {
    private static final int HOME_SPAN_COUNT = 3;
    private static final String SUAN_SUAN_LE_ASSET_URL = "file:///android_asset/suansuanle/index.html";

    private final List<HomeFeatureSpec> defaultFeatureSpecs = new ArrayList<>();
    private final List<HomeFeatureSpec> orderedFeatureSpecs = new ArrayList<>();

    private RecyclerView recyclerView;
    private HomeLauncherAdapter launcherAdapter;
    private HomeFeatureOrderStore featureOrderStore;
    private ItemTouchHelper itemTouchHelper;
    private int contentBasePaddingBottom = Integer.MIN_VALUE;

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

    private void init(Context context) {
        inflate(context, R.layout.main_shop_tab_layout_new, this);
        recyclerView = findViewById(R.id.home_recycler_view);
        featureOrderStore = new HomeFeatureOrderStore(context);
        initFeatureSpecs(context);
        initRecyclerView(context);
        refreshVisibleItems();
    }

    private void initRecyclerView(Context context) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, HOME_SPAN_COUNT);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? HOME_SPAN_COUNT : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        launcherAdapter = new HomeLauncherAdapter(
                context,
                featureSpec -> context.startActivity(featureSpec.buildIntent(context)),
                viewHolder -> itemTouchHelper.startDrag(viewHolder)
        );
        recyclerView.setAdapter(launcherAdapter);
        recyclerView.addItemDecoration(new HomeGridSpacingDecoration(
                context.getResources().getDimensionPixelSize(R.dimen.captain_space_card_gap)));
        itemTouchHelper = new ItemTouchHelper(new HomeDragCallback(launcherAdapter, this::persistVisibleFeatureOrder));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initFeatureSpecs(Context context) {
        defaultFeatureSpecs.clear();
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "info_qr",
                "信息二维码",
                R.drawable.zxing,
                false,
                createIntentFactory(QRActivity.class)
        ));
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "ledongli",
                "乐动力",
                R.drawable.ledongli,
                true,
                ctx -> {
                    Intent intent = new Intent(ctx, LDLWebViewActivity.class);
                    intent.putExtra(WebViewActivity.URL_KEY, "https://market.m.taobao.com/app/alisports-fe/sports-gym-client/h5/index.html");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return intent;
                }
        ));
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "lelidong",
                "乐力动",
                R.drawable.ledongli,
                true,
                ctx -> {
                    Intent intent = new Intent(ctx, LLDWebViewActivity.class);
                    intent.putExtra(WebViewActivity.URL_KEY, "https://market.m.taobao.com/app/alisports-fe/sports-gym-client/h5/index.html");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return intent;
                }
        ));
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "resource_detect",
                "资源检测",
                R.drawable.download,
                false,
                createIntentFactory(XhsDownloadActivity.class)
        ));
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "suansuanle",
                context.getString(R.string.feature_suansuanle_title),
                R.drawable.ic_suansuanle_feature,
                false,
                ctx -> {
                    Intent intent = new Intent(ctx, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.URL_KEY, SUAN_SUAN_LE_ASSET_URL);
                    intent.putExtra(WebViewActivity.TITLE_KEY, ctx.getString(R.string.feature_suansuanle_title));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return intent;
                }
        ));
        defaultFeatureSpecs.add(new HomeFeatureSpec(
                "chess",
                "国际象棋",
                R.drawable.ic_chess_feature,
                true,
                createIntentFactory(ChessLobbyActivity.class)
        ));
    }

    private HomeFeatureSpec.IntentFactory createIntentFactory(Class<?> targetClass) {
        return context -> {
            Intent intent = new Intent(context, targetClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        };
    }

    public void setBottomContentInset(int bottomInset) {
        if (recyclerView == null) {
            return;
        }
        if (contentBasePaddingBottom == Integer.MIN_VALUE) {
            contentBasePaddingBottom = recyclerView.getPaddingBottom();
        }
        int targetPaddingBottom = contentBasePaddingBottom + Math.max(bottomInset, 0);
        if (recyclerView.getPaddingBottom() == targetPaddingBottom) {
            return;
        }
        recyclerView.setPadding(
                recyclerView.getPaddingLeft(),
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight(),
                targetPaddingBottom
        );
    }

    public void refreshVisibleItems() {
        orderedFeatureSpecs.clear();
        orderedFeatureSpecs.addAll(featureOrderStore.loadOrderedSpecs(defaultFeatureSpecs));
        launcherAdapter.submitFeatures(getVisibleFeatureSpecs());
    }

    private void persistVisibleFeatureOrder(@NonNull List<HomeFeatureSpec> reorderedVisibleFeatures) {
        List<String> mergedOrderedIds = HomeFeatureOrderStore.mergeVisibleOrder(
                HomeFeatureSpec.collectIds(orderedFeatureSpecs),
                HomeFeatureSpec.collectIds(reorderedVisibleFeatures)
        );
        orderedFeatureSpecs.clear();
        orderedFeatureSpecs.addAll(HomeFeatureOrderStore.orderSpecs(defaultFeatureSpecs, mergedOrderedIds));
        featureOrderStore.saveOrderedIds(mergedOrderedIds);
    }

    @NonNull
    private List<HomeFeatureSpec> getVisibleFeatureSpecs() {
        List<HomeFeatureSpec> visibleFeatureSpecs = new ArrayList<>();
        boolean hiddenFeaturesEnabled = HiddenFeaturePrefs.isHiddenFeaturesEnabled(getContext());
        for (HomeFeatureSpec orderedFeatureSpec : orderedFeatureSpecs) {
            if (!orderedFeatureSpec.isHidden() || hiddenFeaturesEnabled) {
                visibleFeatureSpecs.add(orderedFeatureSpec);
            }
        }
        return visibleFeatureSpecs;
    }

    private static final class HomeGridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        private HomeGridSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect,
                                   @NonNull View view,
                                   @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            int adapterPosition = parent.getChildAdapterPosition(view);
            if (adapterPosition <= 0) {
                outRect.set(0, 0, 0, 0);
                return;
            }
            int featureIndex = adapterPosition - 1;
            int column = featureIndex % HOME_SPAN_COUNT;
            outRect.left = column * spacing / HOME_SPAN_COUNT;
            outRect.right = spacing - (column + 1) * spacing / HOME_SPAN_COUNT;
            if (featureIndex >= HOME_SPAN_COUNT) {
                outRect.top = spacing;
            }
        }
    }
}
