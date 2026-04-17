package com.shuqiang.captain;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import captain.R;

// 首页桌面面板适配器，负责欢迎区和功能入口的统一渲染。
public final class HomeLauncherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface FeatureActionListener {
        void onFeatureClicked(HomeFeatureSpec featureSpec);
    }

    public interface DragStartListener {
        void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder);
    }

    private static final int VIEW_TYPE_HERO = 0;
    private static final int VIEW_TYPE_FEATURE = 1;
    private static final long HERO_STABLE_ID = Long.MIN_VALUE;

    private final LayoutInflater inflater;
    private final FeatureActionListener actionListener;
    private final DragStartListener dragStartListener;
    private final List<HomeFeatureSpec> featureSpecs = new ArrayList<>();

    public HomeLauncherAdapter(@NonNull Context context,
                               @NonNull FeatureActionListener actionListener,
                               @NonNull DragStartListener dragStartListener) {
        this.inflater = LayoutInflater.from(context);
        this.actionListener = actionListener;
        this.dragStartListener = dragStartListener;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HERO : VIEW_TYPE_FEATURE;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return HERO_STABLE_ID;
        }
        return featureSpecs.get(position - 1).getStableId();
    }

    @Override
    public int getItemCount() {
        return featureSpecs.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HERO) {
            View itemView = inflater.inflate(R.layout.home_hero_item, parent, false);
            return new HeroViewHolder(itemView);
        }
        View itemView = inflater.inflate(R.layout.home_launcher_item, parent, false);
        return new FeatureViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeatureViewHolder) {
            ((FeatureViewHolder) holder).bind(featureSpecs.get(position - 1));
        }
    }

    public boolean isFeaturePosition(int adapterPosition) {
        return adapterPosition > 0 && adapterPosition < getItemCount();
    }

    public void submitFeatures(@NonNull List<HomeFeatureSpec> newFeatureSpecs) {
        featureSpecs.clear();
        featureSpecs.addAll(newFeatureSpecs);
        notifyDataSetChanged();
    }

    public void moveFeature(int fromAdapterPosition, int toAdapterPosition) {
        if (!isFeaturePosition(fromAdapterPosition) || !isFeaturePosition(toAdapterPosition)) {
            return;
        }
        HomeFeatureOrderStore.moveItem(featureSpecs, fromAdapterPosition - 1, toAdapterPosition - 1);
        notifyItemMoved(fromAdapterPosition, toAdapterPosition);
    }

    @NonNull
    public List<HomeFeatureSpec> copyVisibleFeatures() {
        return new ArrayList<>(featureSpecs);
    }

    private final class HeroViewHolder extends RecyclerView.ViewHolder {
        private HeroViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public final class FeatureViewHolder extends RecyclerView.ViewHolder {
        private final View iconTile;
        private final ImageView iconView;
        private final TextView titleView;

        private FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            iconTile = itemView.findViewById(R.id.icon_tile);
            iconView = itemView.findViewById(R.id.icon);
            titleView = itemView.findViewById(R.id.title);
        }

        private void bind(@NonNull HomeFeatureSpec featureSpec) {
            titleView.setText(featureSpec.getTitle());
            iconView.setImageResource(featureSpec.getIconRes());
            iconTile.setBackgroundResource(featureSpec.isHidden()
                    ? R.drawable.bg_icon_tile_hidden
                    : R.drawable.bg_icon_tile);
            itemView.setOnClickListener(v -> actionListener.onFeatureClicked(featureSpec));
            itemView.setOnLongClickListener(v -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                    return false;
                }
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                dragStartListener.onStartDrag(this);
                return true;
            });
        }

        public void setDragging(boolean dragging) {
            float scale = dragging ? 1.04f : 1f;
            float alpha = dragging ? 0.92f : 1f;
            itemView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .alpha(alpha)
                    .setDuration(120L)
                    .start();
            itemView.setElevation(dragging ? itemView.getResources()
                    .getDimension(R.dimen.captain_elevation_tab_shell) : 0f);
        }
    }
}
