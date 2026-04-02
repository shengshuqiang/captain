package com.shuqiang.captain.chess.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shuqiang.captain.chess.model.ChessScannedDevice;

import java.util.ArrayList;
import java.util.List;

import captain.R;

public class ChessDeviceAdapter extends RecyclerView.Adapter<ChessDeviceAdapter.ViewHolder> {
    public interface OnConnectClickListener {
        void onConnectClick(ChessScannedDevice device);
    }

    private final List<ChessScannedDevice> devices = new ArrayList<>();
    private final OnConnectClickListener listener;

    public ChessDeviceAdapter(OnConnectClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChessScannedDevice> items) {
        devices.clear();
        if (items != null) {
            devices.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chess_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChessScannedDevice device = devices.get(position);
        holder.nameView.setText(device.getName());
        holder.addressView.setText(device.getAddress());
        holder.tagView.setText(device.isBonded() ? R.string.chess_bonded : R.string.chess_unbonded);
        holder.connectButton.setOnClickListener(v -> listener.onConnectClick(device));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView addressView;
        private final TextView tagView;
        private final Button connectButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.device_name);
            addressView = itemView.findViewById(R.id.device_address);
            tagView = itemView.findViewById(R.id.device_tag);
            connectButton = itemView.findViewById(R.id.device_connect_button);
        }
    }
}
