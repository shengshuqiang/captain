package com.shuqiang.captain.chess.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.shuqiang.captain.chess.data.ChessAvatarPresets;
import com.shuqiang.captain.chess.data.ChessAvatarStore;
import com.shuqiang.captain.chess.model.ChessAvatarType;
import com.shuqiang.captain.chess.model.ChessUserProfile;

import java.io.File;

import captain.R;

public final class ChessAvatarLoader {
    private ChessAvatarLoader() {
    }

    public static void load(ImageView imageView, ChessAvatarStore avatarStore, ChessUserProfile profile) {
        if (profile == null) {
            Glide.with(imageView).load(R.drawable.avatar_chess_helmet_red).circleCrop().into(imageView);
            return;
        }
        if (profile.getAvatarType() == ChessAvatarType.LOCAL) {
            File avatarFile = avatarStore.resolveAvatarFile(imageView.getContext(), profile.getAvatarValue());
            if (avatarFile != null && avatarFile.exists()) {
                Glide.with(imageView)
                        .load(avatarFile)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_chess_helmet_red)
                        .error(R.drawable.avatar_chess_helmet_red)
                        .into(imageView);
                return;
            }
        }
        Glide.with(imageView)
                .load(ChessAvatarPresets.resolveDrawableRes(profile.getAvatarValue()))
                .circleCrop()
                .into(imageView);
    }
}
