package com.shuqiang.captain.chess.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captain.base.BaseActivity;
import com.shuqiang.captain.chess.ChessManager;
import com.shuqiang.captain.chess.model.ChessAvatarPreset;
import com.shuqiang.captain.chess.model.ChessAvatarType;
import com.shuqiang.captain.chess.model.ChessUserProfile;

import java.util.List;

import captain.R;

public class ChessOnboardingActivity extends BaseActivity {
    private static final int REQUEST_CODE_PICK_AVATAR = 0xC821;
    public static final String EXTRA_EDIT_MODE = "edit_mode";
    private ChessManager chessManager;
    private ImageView avatarPreview;
    private EditText usernameInput;
    private ChessAvatarPresetAdapter avatarPresetAdapter;
    private ChessAvatarType selectedAvatarType;
    private String selectedAvatarValue;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chessManager = ChessManager.getInstance(this);
        editMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chess_onboarding_title);
        }
        avatarPreview = findViewById(R.id.avatar_preview);
        usernameInput = findViewById(R.id.username_input);
        TextView titleView = findViewById(R.id.page_title);
        titleView.setText(editMode ? R.string.chess_profile_card_title : R.string.chess_onboarding_title);
        RecyclerView avatarRecycler = findViewById(R.id.avatar_recycler);
        List<ChessAvatarPreset> presets = chessManager.getAvatarPresets();
        avatarPresetAdapter = new ChessAvatarPresetAdapter(presets, this::selectPresetAvatar);
        avatarRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        avatarRecycler.setAdapter(avatarPresetAdapter);
        Button localAvatarButton = findViewById(R.id.pick_local_avatar_button);
        Button saveButton = findViewById(R.id.save_profile_button);
        localAvatarButton.setOnClickListener(v -> openAvatarPicker());
        saveButton.setText(editMode ? R.string.chess_update_profile : R.string.chess_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());
        bindExistingProfile();
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_chess_onboarding;
    }

    private void bindExistingProfile() {
        ChessUserProfile profile = chessManager.getLocalProfile();
        usernameInput.setText(profile.getUsername());
        selectedAvatarType = profile.getAvatarType();
        selectedAvatarValue = TextUtils.isEmpty(profile.getAvatarValue())
                ? chessManager.getAvatarPresets().get(0).getValue()
                : profile.getAvatarValue();
        if (selectedAvatarType == null) {
            selectedAvatarType = ChessAvatarType.PRESET;
        }
        avatarPresetAdapter.setSelectedValue(selectedAvatarType == ChessAvatarType.PRESET ? selectedAvatarValue : null);
        ChessAvatarLoader.load(avatarPreview, chessManager.getAvatarStore(),
                new ChessUserProfile(profile.getUsername(), selectedAvatarType, selectedAvatarValue));
    }

    private void selectPresetAvatar(ChessAvatarPreset preset) {
        selectedAvatarType = ChessAvatarType.PRESET;
        selectedAvatarValue = preset.getValue();
        avatarPresetAdapter.setSelectedValue(preset.getValue());
        ChessAvatarLoader.load(avatarPreview, chessManager.getAvatarStore(),
                new ChessUserProfile(usernameInput.getText().toString(), selectedAvatarType, selectedAvatarValue));
    }

    private void openAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                selectedAvatarType = ChessAvatarType.LOCAL;
                selectedAvatarValue = chessManager.getAvatarStore().copyAvatarFromUri(this, uri);
                avatarPresetAdapter.setSelectedValue(null);
                ChessAvatarLoader.load(avatarPreview, chessManager.getAvatarStore(),
                        new ChessUserProfile(usernameInput.getText().toString(), selectedAvatarType, selectedAvatarValue));
            } catch (Exception exception) {
                Toast.makeText(this, "头像处理失败: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请先输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedAvatarValue)) {
            selectedAvatarType = ChessAvatarType.PRESET;
            selectedAvatarValue = chessManager.getAvatarPresets().get(0).getValue();
        }
        chessManager.saveProfile(new ChessUserProfile(username, selectedAvatarType, selectedAvatarValue), true);
        if (editMode) {
            finish();
            return;
        }
        startActivity(new Intent(this, ChessLobbyActivity.class));
        finish();
    }
}
