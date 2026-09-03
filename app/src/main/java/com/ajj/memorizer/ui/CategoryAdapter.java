package com.ajj.memorizer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajj.memorizer.R;
import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryDao.CategoryWithStats> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onClick(Category category);
    }

    public CategoryAdapter(List<CategoryDao.CategoryWithStats> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<CategoryDao.CategoryWithStats> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDao.CategoryWithStats item = categories.get(position);
        Category category = item.category;
        
        holder.tvName.setText(category.getName());
        
        int mastery = item.cardCount == 0 ? 0 : (int) (((float) item.masteredCount / item.cardCount) * 100);
        String masteryText = String.format(java.util.Locale.getDefault(), "%d%% Mastery", mastery);
        holder.tvMastery.setText(masteryText);
        
        // Color coding mastery
        if (mastery < 30) holder.tvMastery.setBackgroundColor(0xFFE53935); // Red
        else if (mastery < 70) holder.tvMastery.setBackgroundColor(0xFFFDD835); // Yellow
        else holder.tvMastery.setBackgroundColor(0xFF43A047); // Green
        
        String stats = item.cardCount + " cards • " + item.dueCount + " due";
        holder.tvStats.setText(stats);
        
        int progress = item.cardCount == 0 ? 0 : (int) (((float) (item.cardCount - item.dueCount) / item.cardCount) * 100);
        holder.progressBar.setProgress(progress);
        
        holder.itemView.setOnClickListener(v -> listener.onClick(category));
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStats, tvMastery;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvStats = itemView.findViewById(R.id.tv_stats);
            tvMastery = itemView.findViewById(R.id.tv_mastery_badge);
            progressBar = itemView.findViewById(R.id.progress_category);
        }
    }
}
