package com.shuqiang.captain.chess.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shuqiang.captain.chess.ChessTextFormatter;
import com.shuqiang.captain.chess.data.ChessAvatarStore;
import com.shuqiang.captain.chess.model.ChessGameRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import captain.R;

public class ChessHistoryAdapter extends RecyclerView.Adapter<ChessHistoryAdapter.ViewHolder> {
    public interface OnReplayClickListener {
        void onReplayClick(ChessGameRecord record);
    }

    private final List<ChessGameRecord> records = new ArrayList<>();
    private final ChessAvatarStore avatarStore;
    private final OnReplayClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    public ChessHistoryAdapter(ChessAvatarStore avatarStore, OnReplayClickListener listener) {
        this.avatarStore = avatarStore;
        this.listener = listener;
    }

    public void submitList(List<ChessGameRecord> items) {
        records.clear();
        if (items != null) {
            records.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chess_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChessGameRecord record = records.get(position);
        ChessAvatarLoader.load(holder.avatarView, avatarStore,
                new com.shuqiang.captain.chess.model.ChessUserProfile(
                        record.getOpponentUsername(), record.getOpponentAvatarType(), record.getOpponentAvatarValue()));
        holder.nameView.setText(record.getOpponentUsername());
        holder.timeView.setText(dateFormat.format(new Date(record.getEndedAt())));
        holder.metaView.setText(record.getMyColor().getLabel() + " · "
                + ChessTextFormatter.combineResultAndReason(record.getResult(), record.getTerminationReason(),
                record.getMyColor()));
        holder.detailView.setText(record.getMoveCount() + " 步 · " + ChessTextFormatter.formatTerminationReason(
                record.getTerminationReason()));
        holder.replayButton.setOnClickListener(v -> listener.onReplayClick(record));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView timeView;
        private final TextView metaView;
        private final TextView detailView;
        private final Button replayButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.history_avatar);
            nameView = itemView.findViewById(R.id.history_name);
            timeView = itemView.findViewById(R.id.history_time);
            metaView = itemView.findViewById(R.id.history_meta);
            detailView = itemView.findViewById(R.id.history_detail);
            replayButton = itemView.findViewById(R.id.history_replay_button);
        }
    }
}
