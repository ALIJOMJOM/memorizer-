package com.ajj.memorizer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ajj.memorizer.R;

import android.widget.TextView;

import com.ajj.memorizer.R;
import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.FlashcardDao;

public class StatsFragment extends Fragment {

    private FlashcardDao flashcardDao;
    private TextView tvTotal, tvMastered, tvStageStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        flashcardDao = AppDatabase.getDatabase(requireContext()).flashcardDao();
        tvTotal = view.findViewById(R.id.tv_total_cards);
        tvMastered = view.findViewById(R.id.tv_mastered_cards);
        tvStageStats = view.findViewById(R.id.tv_stage_stats);

        loadStats();

        return view;
    }

    private void loadStats() {
        new Thread(() -> {
            int total = flashcardDao.getTotalCount();
            int mastered = flashcardDao.getMasteredCount();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Stage 1 (1h): ").append(flashcardDao.getCountByStage(1)).append(" cards\n");
            sb.append("Stage 2 (24h): ").append(flashcardDao.getCountByStage(2)).append(" cards\n");
            sb.append("Stage 3 (3d): ").append(flashcardDao.getCountByStage(3)).append(" cards\n");
            sb.append("Stage 4 (7d): ").append(flashcardDao.getCountByStage(4)).append(" cards\n");
            sb.append("Stage 5 (16d+): ").append(flashcardDao.getCountByStage(5)).append(" cards");

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotal.setText(String.valueOf(total));
                    tvMastered.setText(String.valueOf(mastered));
                    tvStageStats.setText(sb.toString());
                });
            }
        }).start();
    }
}
