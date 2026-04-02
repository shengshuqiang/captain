package com.shuqiang.captain.chess.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shuqiang.captain.chess.model.ChessAvatarPreset;

import java.util.List;

import captain.R;

public class ChessAvatarPresetAdapter extends RecyclerView.Adapter<ChessAvatarPresetAdapter.ViewHolder> {
    public interface OnPresetClickListener {
        void onPresetClick(ChessAvatarPreset preset);
    }

    private final List<ChessAvatarPreset> presets;
    private final OnPresetClickListener listener;
    private String selectedValue;

    public ChessAvatarPresetAdapter(List<ChessAvatarPreset> presets, OnPresetClickListener listener) {
        this.presets = presets;
        this.listener = listener;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chess_avatar_preset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChessAvatarPreset preset = presets.get(position);
        holder.avatarView.setImageResource(preset.getDrawableRes());
        holder.nameView.setText(preset.getLabel());
        holder.itemView.setBackgroundResource(preset.getValue().equals(selectedValue)
                ? R.drawable.bg_chess_choice_selected : R.drawable.bg_chess_choice_idle);
        holder.itemView.setOnClickListener(v -> listener.onPresetClick(preset));
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarView;
        private final TextView nameView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar_image);
            nameView = itemView.findViewById(R.id.avatar_name);
        }
    }
}
