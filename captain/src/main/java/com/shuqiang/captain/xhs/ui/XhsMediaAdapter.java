package com.shuqiang.captain.xhs.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.util.ArrayList;
import java.util.List;

import captain.R;

/**
 * 解析结果选择列表，只负责展示和选中态切换。
 */
public class XhsMediaAdapter extends RecyclerView.Adapter<XhsMediaAdapter.MediaViewHolder> {
    public interface OnMediaActionListener {
        void onSelectionChanged();

        void onPreviewRequested(int position);
    }

    private final ArrayList<XhsMediaItem> items = new ArrayList<>();
    private final OnMediaActionListener onMediaActionListener;

    public XhsMediaAdapter(OnMediaActionListener onMediaActionListener) {
        this.onMediaActionListener = onMediaActionListener;
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

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_xhs_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        XhsMediaItem item = items.get(position);
        holder.bind(item, position, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout mediaCard;
        private final ImageView mediaCover;
        private final View selectedMask;
        private final TextView mediaType;
        private final TextView mediaDuration;
        private final TextView mediaIndex;
        private final TextView mediaPreview;
        private final TextView mediaSelected;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaCard = itemView.findViewById(R.id.media_card);
            mediaCover = itemView.findViewById(R.id.media_cover);
            selectedMask = itemView.findViewById(R.id.selected_mask);
            mediaType = itemView.findViewById(R.id.media_type);
            mediaDuration = itemView.findViewById(R.id.media_duration);
            mediaIndex = itemView.findViewById(R.id.media_index);
            mediaPreview = itemView.findViewById(R.id.media_preview);
            mediaSelected = itemView.findViewById(R.id.media_selected);
        }

        void bind(final XhsMediaItem item, int position, int totalCount) {
            String previewUrl = item.getCoverUrl() == null || item.getCoverUrl().isEmpty()
                    ? item.getMediaUrl()
                    : item.getCoverUrl();
            Glide.with(mediaCover.getContext())
                    .load(previewUrl)
                    .into(mediaCover);

            mediaType.setText(item.getMediaType() == XhsMediaType.VIDEO
                    ? mediaCover.getContext().getString(R.string.xhs_download_video_badge)
                    : mediaCover.getContext().getString(R.string.xhs_download_image_badge));
            mediaIndex.setText((position + 1) + " / " + totalCount);
            mediaDuration.setText(item.getDisplayDuration());
            mediaDuration.setVisibility(item.getMediaType() == XhsMediaType.VIDEO
                    && !item.getDisplayDuration().isEmpty() ? View.VISIBLE : View.GONE);
            mediaPreview.setText(item.getMediaType() == XhsMediaType.VIDEO
                    ? mediaCover.getContext().getString(R.string.xhs_download_play)
                    : mediaCover.getContext().getString(R.string.xhs_download_preview));

            selectedMask.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
            mediaCard.setBackgroundResource(item.isSelected()
                    ? R.drawable.bg_xhs_media_card_selected
                    : R.drawable.bg_xhs_media_card);
            mediaSelected.setText(item.isSelected()
                    ? mediaSelected.getContext().getString(R.string.xhs_download_selected)
                    : mediaSelected.getContext().getString(R.string.xhs_download_select));
            mediaSelected.setBackgroundResource(item.isSelected()
                    ? R.drawable.bg_xhs_selected_badge
                    : R.drawable.bg_xhs_selection_idle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getBindingAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    if (onMediaActionListener != null) {
                        onMediaActionListener.onPreviewRequested(adapterPosition);
                    }
                }
            });
            mediaSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getBindingAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    item.setSelected(!item.isSelected());
                    notifyItemChanged(adapterPosition);
                    if (onMediaActionListener != null) {
                        onMediaActionListener.onSelectionChanged();
                    }
                }
            });
        }
    }
}
