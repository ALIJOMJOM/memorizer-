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
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.ui.model.LibraryItem;

import java.util.List;
import java.util.Locale;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_FILE = 1;

    public interface OnItemClickListener {
        void onFolderClick(Category category);
        void onFileEdit(Flashcard flashcard);
        void onFileDelete(Flashcard flashcard);
    }

    private List<LibraryItem> items;
    private final OnItemClickListener listener;

    public LibraryAdapter(List<LibraryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<LibraryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == LibraryItem.Type.FOLDER ? TYPE_FOLDER : TYPE_FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new FolderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
            return new FileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LibraryItem item = items.get(position);
        if (holder instanceof FolderViewHolder) {
            bindFolder((FolderViewHolder) holder, item.getCategoryWithStats());
        } else {
            bindFile((FileViewHolder) holder, item.getFlashcard(), position);
        }
    }

    private void bindFolder(FolderViewHolder holder, CategoryDao.CategoryWithStats stats) {
        Category category = stats.category;
        holder.tvName.setText(category.getName());
        
        int mastery = stats.cardCount == 0 ? 0 : (int) (((float) stats.masteredCount / stats.cardCount) * 100);
        holder.tvMastery.setText(String.format(Locale.getDefault(), "%d%% Mastery", mastery));
        
        if (mastery < 30) holder.tvMastery.setBackgroundColor(0xFFE53935);
        else if (mastery < 70) holder.tvMastery.setBackgroundColor(0xFFFDD835);
        else holder.tvMastery.setBackgroundColor(0xFF43A047);
        
        holder.tvStats.setText(String.format(Locale.getDefault(), "%d cards • %d due", stats.cardCount, stats.dueCount));
        int progress = stats.cardCount == 0 ? 0 : (int) (((float) (stats.cardCount - stats.dueCount) / stats.cardCount) * 100);
        holder.progressBar.setProgress(progress);
        
        holder.itemView.setOnClickListener(v -> listener.onFolderClick(category));
    }

    private void bindFile(FileViewHolder holder, Flashcard card, int position) {
        String question = (position + 1) + ". " + card.getQuestion();
        holder.tvQuestion.setText(question);
        holder.tvAnswer.setText(card.getAnswer());
        holder.tvCategory.setVisibility(View.GONE); // Already inside category
        
        holder.btnEdit.setOnClickListener(v -> listener.onFileEdit(card));
        holder.btnDelete.setOnClickListener(v -> listener.onFileDelete(card));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStats, tvMastery;
        ProgressBar progressBar;

        FolderViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvStats = itemView.findViewById(R.id.tv_stats);
            tvMastery = itemView.findViewById(R.id.tv_mastery_badge);
            progressBar = itemView.findViewById(R.id.progress_category);
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer, tvCategory;
        View btnEdit, btnDelete;

        FileViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
