package com.shuqiang.captain;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

// 首页入口拖拽回调，只允许真实功能入口之间进行同页重排。
public final class HomeDragCallback extends ItemTouchHelper.SimpleCallback {
    public interface DragFinishedListener {
        void onDragFinished(@NonNull java.util.List<HomeFeatureSpec> reorderedVisibleFeatures);
    }

    private final HomeLauncherAdapter adapter;
    private final DragFinishedListener dragFinishedListener;
    private boolean orderChanged;

    public HomeDragCallback(@NonNull HomeLauncherAdapter adapter,
                            @NonNull DragFinishedListener dragFinishedListener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        this.adapter = adapter;
        this.dragFinishedListener = dragFinishedListener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return adapter.isFeaturePosition(viewHolder.getAdapterPosition())
                ? makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0)
                : 0;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (!adapter.isFeaturePosition(fromPosition) || !adapter.isFeaturePosition(toPosition)) {
            return false;
        }
        adapter.moveFeature(fromPosition, toPosition);
        orderChanged = true;
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 不支持滑动删除。
    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView,
                               @NonNull RecyclerView.ViewHolder current,
                               @NonNull RecyclerView.ViewHolder target) {
        return adapter.isFeaturePosition(target.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG
                && viewHolder instanceof HomeLauncherAdapter.FeatureViewHolder) {
            ((HomeLauncherAdapter.FeatureViewHolder) viewHolder).setDragging(true);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof HomeLauncherAdapter.FeatureViewHolder) {
            ((HomeLauncherAdapter.FeatureViewHolder) viewHolder).setDragging(false);
        }
        super.clearView(recyclerView, viewHolder);
        if (!orderChanged) {
            return;
        }
        orderChanged = false;
        dragFinishedListener.onDragFinished(adapter.copyVisibleFeatures());
    }
}
