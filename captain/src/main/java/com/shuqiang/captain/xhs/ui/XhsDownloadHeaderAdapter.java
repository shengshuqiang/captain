package com.shuqiang.captain.xhs.ui;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** 将已有输入表单作为单个列表头，避免 ScrollView 无界测量所有资源行。 */
final class XhsDownloadHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final View header;

    XhsDownloadHeaderAdapter(View header) {
        this.header = header;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(header) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 表单状态与监听器仍由 Activity 持有，不在滚动重绑时重置。
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
