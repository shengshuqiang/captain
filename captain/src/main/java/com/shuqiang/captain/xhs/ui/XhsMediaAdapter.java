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
public class XhsMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "XhsMediaAdapter";
    private static final String SELECTION_PAYLOAD = "selection";

    public interface OnMediaActionListener {
        void onSelectionChanged(XhsMediaItem item);
        void onCategorySelectionChanged(List<XhsMediaItem> items);
        void onPreviewRequested(int position);
    }

    private final XhsMediaGroups groups = new XhsMediaGroups();
    private final ArrayList<XhsMediaItem> items = groups.getItems();
    private final OnMediaActionListener listener;
    private boolean selectionEnabled = true;

    public XhsMediaAdapter(OnMediaActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<XhsMediaItem> mediaItems) {
        groups.setItems(mediaItems);
        notifyDataSetChanged();
    }

    /** 新解析恢复默认折叠；同页面实时更新保留用户的展开状态。 */
    public void resetExpansion() { groups.resetExpansion(); }

    public void setSelectionEnabled(boolean enabled) {
        if (selectionEnabled == enabled) return;
        selectionEnabled = enabled;
        notifySelectionChanged();
    }

    public ArrayList<XhsMediaItem> getItems() {
        return items;
    }

    /** 全选只更新选择状态，不重新创建资源列表。 */
    public void notifySelectionChanged() {
        notifyItemRangeChanged(0, getItemCount(), SELECTION_PAYLOAD);
    }

    @Override
    public int getItemViewType(int position) {
        return groups.getRows().get(position).isHeader() ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) return new GroupViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_xhs_media_group, parent, false));
        return new MediaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_xhs_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        XhsMediaGroups.Row row = groups.getRows().get(position);
        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).bind(row.category);
        } else {
            ((MediaViewHolder) holder).bind(items.get(row.mediaPosition), row.mediaPosition);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.contains(SELECTION_PAYLOAD) && holder instanceof MediaViewHolder) {
            ((MediaViewHolder) holder).bindSelection(items.get(groups.getRows().get(position).mediaPosition));
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return groups.getRows().size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof MediaViewHolder) {
            Glide.with(((MediaViewHolder) holder).thumbnail).clear(((MediaViewHolder) holder).thumbnail);
        }
        super.onViewRecycled(holder);
    }

    /** 折叠仅减少显示行；分类全选也包含折叠后隐藏的资源。 */
    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView selection;

        GroupViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.media_group_title);
            selection = view.findViewById(R.id.media_group_selection);
            title.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                groups.toggleExpansion(groups.getRows().get(position).category);
                notifyDataSetChanged();
            });
            selection.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (!selectionEnabled || position == RecyclerView.NO_POSITION) return;
                List<XhsMediaItem> changed = groups.toggleSelection(groups.getRows().get(position).category);
                notifySelectionChanged();
                if (listener != null) listener.onCategorySelectionChanged(changed);
            });
        }

        void bind(XhsMediaGroups.Category category) {
            int total = groups.count(category, false);
            int selectedCount = groups.count(category, true);
            boolean collapsed = groups.isCollapsed(category);
            title.setText((collapsed ? "▶ " : "▼ ") + category.title + " · " + total + " 项"
                    + "\n" + category.hint + " · 已选 " + selectedCount);
            title.setContentDescription((collapsed ? "展开" : "收起") + category.title
                    + "，共 " + total + " 项，已选 " + selectedCount + " 项");
            selection.setText(selectedCount == total ? "取消全选" : "全选");
            selection.setContentDescription((selectedCount == total ? "取消全选" : "全选") + category.title);
            selection.setEnabled(selectionEnabled);
        }
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
                if (!selectionEnabled || position == RecyclerView.NO_POSITION) return;
                XhsMediaItem item = items.get(groups.getRows().get(position).mediaPosition);
                item.setSelected(!item.isSelected());
                notifySelectionChanged();
                if (listener != null) listener.onSelectionChanged(item);
            });
            preview.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPreviewRequested(groups.getRows().get(position).mediaPosition);
                }
            });
            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showSourceInfo(items.get(groups.getRows().get(position).mediaPosition));
                }
            });
        }

        void bind(XhsMediaItem item, int position) {
            bindThumbnail(item);
            String format = item.getMediaType() == XhsMediaType.VIDEO
                    ? item.getTransport().getDisplayName() : item.getFileExtension();
            type.setText((position + 1) + " · " + item.getMediaType().getDisplayName()
                    + (format == null || format.isEmpty() ? "" : " · " + format));
            source.setText((item.getWidth() > 0 && item.getHeight() > 0
                    ? item.getWidth() + " × " + item.getHeight() : "分辨率未知")
                    + (item.getDisplayDuration().isEmpty() ? "" : " · " + item.getDisplayDuration())
                    + (item.isCurrentPlayback() ? " · 当前播放" : ""));
            address.setText("点击卡片查看来源与完整地址");
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
            selected.setEnabled(selectionEnabled);
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
        LazyHeaders.Builder headers = new LazyHeaders.Builder();
        if (!item.requiresRuntimeSession()) {
            headers.setHeader("User-Agent", com.shuqiang.captain.xhs.parser.XhsHttpClient.MOBILE_USER_AGENT);
            if (item.getSourcePageUrl() != null) headers.setHeader("Referer", item.getSourcePageUrl());
            return new GlideUrl(imageUrl, headers.build());
        }
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
