package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

// 首页功能入口规格，统一承载稳定 id、展示信息和启动逻辑。
public final class HomeFeatureSpec {
    public interface IntentFactory {
        Intent create(Context context);
    }

    private final String id;
    private final String title;
    private final int iconRes;
    private final boolean hidden;
    private final IntentFactory intentFactory;

    public HomeFeatureSpec(@NonNull String id,
                           @NonNull String title,
                           int iconRes,
                           boolean hidden,
                           @NonNull IntentFactory intentFactory) {
        this.id = id;
        this.title = title;
        this.iconRes = iconRes;
        this.hidden = hidden;
        this.intentFactory = intentFactory;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean isHidden() {
        return hidden;
    }

    @NonNull
    public Intent buildIntent(@NonNull Context context) {
        return intentFactory.create(context);
    }

    public long getStableId() {
        long hash = 1469598103934665603L;
        for (int i = 0; i < id.length(); i++) {
            hash ^= id.charAt(i);
            hash *= 1099511628211L;
        }
        return hash;
    }

    @NonNull
    public static List<String> collectIds(@NonNull List<HomeFeatureSpec> featureSpecs) {
        List<String> ids = new ArrayList<>(featureSpecs.size());
        for (HomeFeatureSpec featureSpec : featureSpecs) {
            ids.add(featureSpec.getId());
        }
        return ids;
    }
}
