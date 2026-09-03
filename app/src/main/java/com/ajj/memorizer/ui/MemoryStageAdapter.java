package com.ajj.memorizer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajj.memorizer.R;
import com.ajj.memorizer.ui.model.MemoryStage;

import java.util.List;

public class MemoryStageAdapter extends RecyclerView.Adapter<MemoryStageAdapter.ViewHolder> {

    private List<MemoryStage> stages;

    public MemoryStageAdapter(List<MemoryStage> stages) {
        this.stages = stages;
    }

    public void setStages(List<MemoryStage> stages) {
        this.stages = stages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory_stage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemoryStage stage = stages.get(position);
        holder.tvTitle.setText(stage.getTitle());
        holder.tvInterval.setText(stage.getInterval());
        holder.tvCount.setText(stage.getCardCount() + " cards");
    }

    @Override
    public int getItemCount() {
        return stages == null ? 0 : stages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInterval, tvCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_stage_title);
            tvInterval = itemView.findViewById(R.id.tv_stage_interval);
            tvCount = itemView.findViewById(R.id.tv_stage_count);
        }
    }
}
