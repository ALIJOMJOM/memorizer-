package com.ajj.memorizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajj.memorizer.data.Flashcard;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onEdit(Flashcard card);
        void onDelete(Flashcard card);
    }

    private List<Flashcard> cards;
    private OnActionClickListener listener;

    public FlashcardAdapter(List<Flashcard> cards, OnActionClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    public void setCards(List<Flashcard> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard card = cards.get(position);
        holder.tvQuestion.setText(card.getQuestion());
        holder.tvAnswer.setText(card.getAnswer());
        holder.tvCategory.setText("Cat ID: " + card.getCategoryId());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(card));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(card));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer, tvCategory;
        View btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
