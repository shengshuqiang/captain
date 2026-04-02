package com.shuqiang.captain.chess.data;

import com.shuqiang.captain.chess.model.ChessAvatarPreset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import captain.R;

public final class ChessAvatarPresets {
    public static final String DEFAULT_VALUE = "avatar_chess_helmet_red";

    private ChessAvatarPresets() {
    }

    public static List<ChessAvatarPreset> getAll() {
        List<ChessAvatarPreset> presets = new ArrayList<>();
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_red", "红色赛车帽", R.drawable.avatar_chess_helmet_red));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_blue", "蓝色闪电镜", R.drawable.avatar_chess_helmet_blue));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_green", "绿色冲刺盔", R.drawable.avatar_chess_helmet_green));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_yellow", "黄色星星盔", R.drawable.avatar_chess_helmet_yellow));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_purple", "紫色机械镜", R.drawable.avatar_chess_helmet_purple));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_orange", "橙色冲线手", R.drawable.avatar_chess_helmet_orange));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_mint", "薄荷维修手", R.drawable.avatar_chess_helmet_mint));
        presets.add(new ChessAvatarPreset("avatar_chess_helmet_coral", "珊瑚旗语手", R.drawable.avatar_chess_helmet_coral));
        return Collections.unmodifiableList(presets);
    }

    public static int resolveDrawableRes(String value) {
        for (ChessAvatarPreset preset : getAll()) {
            if (preset.getValue().equals(value)) {
                return preset.getDrawableRes();
            }
        }
        return R.drawable.avatar_chess_helmet_red;
    }
}
