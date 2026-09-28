package com.shuqiang.captain.xhs.ui;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.shuqiang.captain.xhs.download.RuntimeMediaSessionStore;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.util.ArrayList;
import java.util.List;

import captain.R;

/** 图片与明确的视频封面只在列表行可见时加载；视频流本身仍需用户点击播放。 */
public class XhsMediaAdapter extends RecyclerView.Adapter<XhsMediaAdapter.MediaViewHolder> {
    private static final String TAG = "XhsMediaAdapter";
    private static final String SELECTION_PAYLOAD = "selection";

    public interface OnMediaActionListener {
        void onSelectionChanged(XhsMediaItem item);
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

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        Glide.with(holder.thumbnail).clear(holder.thumbnail);
        super.onViewRecycled(holder);
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView type;
        private final TextView source;
        private final TextView address;
        private final TextView preview;
        private final TextView selected;

        MediaViewHolder(@NonNull View view) {
            super(view);
            thumbnail = view.findViewById(R.id.media_thumbnail);
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
                if (listener != null) listener.onSelectionChanged(item);
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
            bindThumbnail(item);
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

        /** 不探测视频流；HLS 卡片只使用页面明确提供的 poster。 */
        private void bindThumbnail(XhsMediaItem item) {
            Glide.with(thumbnail).clear(thumbnail);
            String imageUrl = item.getMediaType() == XhsMediaType.IMAGE
                    ? item.getMediaUrl() : item.getCoverUrl();
            if (item.getMediaType() == XhsMediaType.PDF
                    || imageUrl == null || imageUrl.isEmpty()
                    || item.getMediaType() == XhsMediaType.VIDEO
                    && (imageUrl.equals(item.getMediaUrl())
                    || !(imageUrl.startsWith("https://") || imageUrl.startsWith("http://")))) {
                thumbnail.setVisibility(View.GONE);
                return;
            }
            thumbnail.setVisibility(View.VISIBLE);
            thumbnail.setContentDescription(item.getMediaType() == XhsMediaType.VIDEO
                    ? "页面提供的视频封面" : "图片缩略图");
            Glide.with(thumbnail)
                    .load(thumbnailModel(item, imageUrl))
                    .override(480, 270)
                    .centerCrop()
                    .into(thumbnail);
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

    /** 图片复用浏览会话请求头；封面按自己的域名取 Cookie，避免跨域发送视频 Cookie。 */
    private GlideUrl thumbnailModel(XhsMediaItem item, String imageUrl) {
        if (!item.requiresRuntimeSession()) return new GlideUrl(imageUrl);
        LazyHeaders.Builder headers = new LazyHeaders.Builder();
        if (imageUrl.equals(item.getMediaUrl())) {
            try {
                for (java.util.Map.Entry<String, String> header :
                        RuntimeMediaSessionStore.getInstance().previewHeaders(item).entrySet()) {
                    headers.setHeader(header.getKey(), header.getValue());
                }
            } catch (IllegalStateException error) {
                Log.w(TAG, "thumbnail session expired, host=" + describeHost(imageUrl));
            }
        } else {
            if (item.getSourcePageUrl() != null) headers.setHeader("Referer", item.getSourcePageUrl());
            String cookie = CookieManager.getInstance().getCookie(imageUrl);
            if (cookie != null && !cookie.isEmpty()) headers.setHeader("Cookie", cookie);
        }
        return new GlideUrl(imageUrl, headers.build());
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
