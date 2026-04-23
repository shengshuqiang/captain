package com.shuqiang.captain;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 首页入口顺序只做本地持久化，避免拖拽结果被刷新或重启覆盖。
public final class HomeFeatureOrderStore {
    private static final String PREFS_NAME = "captain_home_feature_order";
    private static final String KEY_FEATURE_ORDER = "home_feature_order";
    private static final String KEY_FEATURE_ORDER_MIGRATION_VERSION = "home_feature_order_migration_version";
    private static final int MIGRATION_VERSION_PIN_FIRST_ENTRY = 1;

    private final SharedPreferences preferences;

    public HomeFeatureOrderStore(@NonNull Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public List<String> loadOrderedIds() {
        String value = preferences.getString(KEY_FEATURE_ORDER, "");
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        List<String> orderedIds = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                String id = jsonArray.optString(i);
                if (!TextUtils.isEmpty(id)) {
                    orderedIds.add(id);
                }
            }
        } catch (JSONException ignore) {
            return new ArrayList<>();
        }
        return orderedIds;
    }

    public void saveOrderedIds(@NonNull List<String> orderedIds) {
        preferences.edit().putString(KEY_FEATURE_ORDER, serializeOrderedIds(orderedIds)).apply();
    }

    private static String serializeOrderedIds(@NonNull List<String> orderedIds) {
        JSONArray jsonArray = new JSONArray();
        for (String orderedId : new LinkedHashSet<>(orderedIds)) {
            jsonArray.put(orderedId);
        }
        return jsonArray.toString();
    }

    @NonNull
    public List<HomeFeatureSpec> loadOrderedSpecs(@NonNull List<HomeFeatureSpec> defaultSpecs) {
        return orderSpecs(defaultSpecs, resolveOrder(HomeFeatureSpec.collectIds(defaultSpecs), loadOrderedIds()));
    }

    @NonNull
    public List<HomeFeatureSpec> loadOrderedSpecs(@NonNull List<HomeFeatureSpec> defaultSpecs,
                                                  @Nullable String pinnedFirstId) {
        List<String> defaultIds = HomeFeatureSpec.collectIds(defaultSpecs);
        List<String> orderedIds = resolveOrder(defaultIds, loadOrderedIds());
        if (shouldApplyPinnedFirstMigration(defaultIds, pinnedFirstId)) {
            orderedIds = resolveOrderWithPinnedFirst(defaultIds, orderedIds, pinnedFirstId);
            preferences.edit()
                    .putString(KEY_FEATURE_ORDER, serializeOrderedIds(orderedIds))
                    .putInt(KEY_FEATURE_ORDER_MIGRATION_VERSION, MIGRATION_VERSION_PIN_FIRST_ENTRY)
                    .apply();
        }
        return orderSpecs(defaultSpecs, orderedIds);
    }

    private boolean shouldApplyPinnedFirstMigration(@NonNull List<String> defaultIds,
                                                   @Nullable String pinnedFirstId) {
        return !TextUtils.isEmpty(pinnedFirstId)
                && defaultIds.contains(pinnedFirstId)
                && preferences.getInt(KEY_FEATURE_ORDER_MIGRATION_VERSION, 0)
                < MIGRATION_VERSION_PIN_FIRST_ENTRY;
    }

    @NonNull
    public static List<String> resolveOrder(@NonNull List<String> defaultIds,
                                            @NonNull List<String> storedIds) {
        Set<String> defaultIdSet = new LinkedHashSet<>(defaultIds);
        LinkedHashSet<String> resolvedIds = new LinkedHashSet<>();
        for (String storedId : storedIds) {
            if (defaultIdSet.contains(storedId)) {
                resolvedIds.add(storedId);
            }
        }
        resolvedIds.addAll(defaultIds);
        return new ArrayList<>(resolvedIds);
    }

    @NonNull
    public static List<String> resolveOrderWithPinnedFirst(@NonNull List<String> defaultIds,
                                                           @NonNull List<String> storedIds,
                                                           @Nullable String pinnedFirstId) {
        List<String> resolvedIds = resolveOrder(defaultIds, storedIds);
        if (pinnedFirstId == null
                || pinnedFirstId.length() == 0
                || !defaultIds.contains(pinnedFirstId)) {
            return resolvedIds;
        }
        resolvedIds.remove(pinnedFirstId);
        resolvedIds.add(0, pinnedFirstId);
        return resolvedIds;
    }

    @NonNull
    public static List<String> mergeVisibleOrder(@NonNull List<String> allOrderedIds,
                                                 @NonNull List<String> visibleOrderedIds) {
        if (visibleOrderedIds.isEmpty()) {
            return new ArrayList<>(allOrderedIds);
        }
        List<String> mergedIds = new ArrayList<>(allOrderedIds);
        Set<String> visibleIdSet = new LinkedHashSet<>(visibleOrderedIds);
        int visibleIndex = 0;
        for (int i = 0; i < mergedIds.size(); i++) {
            if (!visibleIdSet.contains(mergedIds.get(i))) {
                continue;
            }
            if (visibleIndex >= visibleOrderedIds.size()) {
                break;
            }
            mergedIds.set(i, visibleOrderedIds.get(visibleIndex));
            visibleIndex++;
        }
        return mergedIds;
    }

    @NonNull
    public static List<HomeFeatureSpec> orderSpecs(@NonNull List<HomeFeatureSpec> defaultSpecs,
                                                   @NonNull List<String> orderedIds) {
        Map<String, HomeFeatureSpec> specMap = new LinkedHashMap<>();
        for (HomeFeatureSpec defaultSpec : defaultSpecs) {
            specMap.put(defaultSpec.getId(), defaultSpec);
        }
        List<HomeFeatureSpec> orderedSpecs = new ArrayList<>();
        for (String orderedId : orderedIds) {
            HomeFeatureSpec featureSpec = specMap.remove(orderedId);
            if (featureSpec != null) {
                orderedSpecs.add(featureSpec);
            }
        }
        orderedSpecs.addAll(specMap.values());
        return orderedSpecs;
    }

    public static <T> void moveItem(@NonNull List<T> items, int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0
                || fromIndex >= items.size() || toIndex >= items.size()
                || fromIndex == toIndex) {
            return;
        }
        if (fromIndex < toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                java.util.Collections.swap(items, i, i + 1);
            }
            return;
        }
        for (int i = fromIndex; i > toIndex; i--) {
            java.util.Collections.swap(items, i, i - 1);
        }
    }
}
