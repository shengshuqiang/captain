package com.shuqiang.captain.xhs.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.util.ArrayList;
import java.util.List;

import captain.R;

/** 资源清单只展示已知信息；列表绑定、滚动和勾选均不请求媒体或封面。 */
public class XhsMediaAdapter extends RecyclerView.Adapter<XhsMediaAdapter.MediaViewHolder> {
    private static final String SELECTION_PAYLOAD = "selection";

    public interface OnMediaActionListener {
        void onSelectionChanged();
        void onPreviewRequested(int position);
    }

    private final ArrayList<XhsMediaItem> items = new ArrayList<>();
    private final OnMediaActionListener listener;

    public XhsMediaAdapter(OnMediaActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<XhsMediaItem> mediaItems) {
        items.clear();
        if (mediaItems != null) {
            items.addAll(mediaItems);
        }
        notifyDataSetChanged();
    }

    public ArrayList<XhsMediaItem> getItems() {
        return items;
    }

    /** 全选只更新选择状态，不重新创建资源列表。 */
    public void notifySelectionChanged() {
        notifyItemRangeChanged(0, items.size(), SELECTION_PAYLOAD);
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MediaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_xhs_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.contains(SELECTION_PAYLOAD)) {
            holder.bindSelection(items.get(position));
        } else {
            holder.bind(items.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        private final TextView type;
        private final TextView source;
        private final TextView address;
        private final TextView preview;
        private final TextView selected;

        MediaViewHolder(@NonNull View view) {
            super(view);
            type = view.findViewById(R.id.media_type);
            source = view.findViewById(R.id.media_source);
            address = view.findViewById(R.id.media_address);
            preview = view.findViewById(R.id.media_preview);
            selected = view.findViewById(R.id.media_selected);
            selected.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                XhsMediaItem item = items.get(position);
                item.setSelected(!item.isSelected());
                notifyItemChanged(position, SELECTION_PAYLOAD);
                if (listener != null) listener.onSelectionChanged();
            });
            preview.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPreviewRequested(position);
                }
            });
            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) showSourceInfo(items.get(position));
            });
        }

        void bind(XhsMediaItem item, int position) {
            String format = item.getMediaType() == XhsMediaType.VIDEO
                    ? item.getTransport().getDisplayName() : item.getFileExtension();
            type.setText((position + 1) + " · " + item.getMediaType().getDisplayName()
                    + (format == null || format.isEmpty() ? "" : " · " + format));
            source.setText("来源：" + describeHost(item.getMediaUrl())
                    + (item.getWidth() > 0 && item.getHeight() > 0
                    ? " · " + item.getWidth() + " × " + item.getHeight() : " · 分辨率未知")
                    + (item.getDiscoveredAtMs() > 0 ? "\n发现于 "
                    + new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date(item.getDiscoveredAtMs())) : "")
                    + (item.isCurrentPlayback() ? " · 当前播放" : ""));
            address.setText(item.getMediaUrl());
            preview.setText(item.getMediaType() == XhsMediaType.VIDEO ? "播放" : "预览");
            bindSelection(item);
        }

        void bindSelection(XhsMediaItem item) {
            itemView.setBackgroundResource(item.isSelected()
                    ? R.drawable.bg_xhs_media_card_selected : R.drawable.bg_xhs_media_card);
            selected.setText(item.isSelected() ? R.string.xhs_download_selected : R.string.xhs_download_select);
            selected.setBackgroundResource(item.isSelected()
                    ? R.drawable.bg_xhs_selected_badge : R.drawable.bg_xhs_selection_idle);
            selected.setContentDescription(item.isSelected() ? "取消选择此资源" : "选择此资源");
        }

        /** 完整地址仅在用户主动查看时展示，不为补齐信息发起网络探测。 */
        private void showSourceInfo(XhsMediaItem item) {
            String page = item.getSourcePageUrl();
            AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                    .setTitle("资源来源")
                    .setMessage("类型：" + item.getMediaType().getDisplayName()
                            + "\n资源地址：\n" + item.getMediaUrl()
                            + (page == null || page.isEmpty() ? "" : "\n\n来源页面：\n" + page)
                            + "\n\n时长：" + (item.getDisplayDuration().isEmpty() ? "未知" : item.getDisplayDuration())
                            + "\n文件大小：尚未请求\n广告属性及流是否可分离：未知\n点击预览或勾选保存后才加载资源。")
                    .setPositiveButton("关闭", null).show();
            TextView message = dialog.findViewById(android.R.id.message);
            if (message != null) message.setTextIsSelectable(true);
        }
    }

    private static String describeHost(String url) {
        try {
            String host = Uri.parse(url).getHost();
            return host == null ? "未知" : host;
        } catch (RuntimeException ignored) {
            return "未知";
        }
    }
}
