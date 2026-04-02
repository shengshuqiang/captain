package com.shuqiang.captain.chess.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.shuqiang.captain.chess.model.ChessAvatarType;
import com.shuqiang.captain.chess.model.ChessSeatPreference;
import com.shuqiang.captain.chess.model.ChessUserProfile;

public class ChessPrefsStore {
    private static final String PREFS_NAME = "captain_chess_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_TYPE = "avatar_type";
    private static final String KEY_AVATAR_VALUE = "avatar_value";
    private static final String KEY_LAST_SEAT_PREFERENCE = "last_seat_preference";
    private static final String KEY_LAST_DEVICE_MAC = "last_device_mac";
    private static final String KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding";

    private final SharedPreferences preferences;

    public ChessPrefsStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public ChessUserProfile loadProfile() {
        String username = preferences.getString(KEY_USERNAME, "");
        String avatarValue = preferences.getString(KEY_AVATAR_VALUE, ChessAvatarPresets.DEFAULT_VALUE);
        ChessAvatarType avatarType = ChessAvatarType.fromValue(
                preferences.getString(KEY_AVATAR_TYPE, ChessAvatarType.PRESET.name()));
        if (TextUtils.isEmpty(avatarValue)) {
            avatarValue = ChessAvatarPresets.DEFAULT_VALUE;
        }
        return new ChessUserProfile(username, avatarType, avatarValue);
    }

    public void saveProfile(ChessUserProfile profile, boolean hasCompletedOnboarding) {
        preferences.edit()
                .putString(KEY_USERNAME, profile.getUsername())
                .putString(KEY_AVATAR_TYPE, profile.getAvatarType().name())
                .putString(KEY_AVATAR_VALUE, profile.getAvatarValue())
                .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, hasCompletedOnboarding)
                .apply();
    }

    public boolean hasCompletedOnboarding() {
        return preferences.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
                && !TextUtils.isEmpty(preferences.getString(KEY_USERNAME, ""));
    }

    public ChessSeatPreference getLastSeatPreference() {
        return ChessSeatPreference.fromValue(preferences.getString(KEY_LAST_SEAT_PREFERENCE,
                ChessSeatPreference.RANDOM.name()));
    }

    public void setLastSeatPreference(ChessSeatPreference preference) {
        preferences.edit().putString(KEY_LAST_SEAT_PREFERENCE, preference.name()).apply();
    }

    public String getLastDeviceMac() {
        return preferences.getString(KEY_LAST_DEVICE_MAC, "");
    }

    public void setLastDeviceMac(String deviceMac) {
        preferences.edit().putString(KEY_LAST_DEVICE_MAC, deviceMac).apply();
    }
}
