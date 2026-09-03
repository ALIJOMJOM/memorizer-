package com.ajj.memorizer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajj.memorizer.R;
import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;
import com.ajj.memorizer.data.FlashcardDao;
import com.ajj.memorizer.ui.model.MemoryStage;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private FlashcardDao flashcardDao;
    private CategoryDao categoryDao;
    private TextView tvTotal, tvMastered;
    private Spinner spinnerCategory;
    private ChipGroup chipGroupTime;
    private RecyclerView rvStages;
    private MemoryStageAdapter stageAdapter;
    
    private List<Category> allCategories = new ArrayList<>();
    private Integer selectedCategoryId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        flashcardDao = db.flashcardDao();
        categoryDao = db.categoryDao();

        tvTotal = view.findViewById(R.id.tv_total_cards);
        tvMastered = view.findViewById(R.id.tv_mastered_cards);
        spinnerCategory = view.findViewById(R.id.spinner_stats_category);
        chipGroupTime = view.findViewById(R.id.chip_group_time);
        rvStages = view.findViewById(R.id.rv_memory_stages);

        rvStages.setLayoutManager(new LinearLayoutManager(requireContext()));
        stageAdapter = new MemoryStageAdapter(new ArrayList<>());
        rvStages.setAdapter(stageAdapter);

        setupFilters();

        return view;
    }

    private void setupFilters() {
        categoryDao.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategories = categories;
                List<String> names = new ArrayList<>();
                names.add("All Categories");
                for (Category c : categories) names.add(c.getName());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = (position == 0) ? null : allCategories.get(position - 1).getId();
                loadStats();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> loadStats());
    }

    private void loadStats() {
        new Thread(() -> {
            long timeLimit = Long.MAX_VALUE;
            int checkedId = chipGroupTime.getCheckedChipId();
            if (checkedId == R.id.chip_today) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                timeLimit = cal.getTimeInMillis();
            } else if (checkedId == R.id.chip_pending) {
                timeLimit = System.currentTimeMillis();
            }

            List<Integer> targetIds = new ArrayList<>();
            if (selectedCategoryId == null) {
                List<Category> cats = categoryDao.getAllCategoriesSync();
                for (Category c : cats) targetIds.add(c.getId());
            } else {
                targetIds = categoryDao.getDescendantIds(selectedCategoryId);
            }

            int total, mastered;
            List<MemoryStage> stageList = new ArrayList<>();
            
            if (targetIds.isEmpty()) {
                total = 0; mastered = 0;
            } else {
                total = flashcardDao.getTotalCountInHierarchy(targetIds, timeLimit);
                mastered = flashcardDao.getMasteredCountInHierarchy(targetIds, timeLimit);
                
                stageList.add(new MemoryStage("Stage 1", flashcardDao.getCountByStageInHierarchy(targetIds, 1, timeLimit), "1 hour recall"));
                stageList.add(new MemoryStage("Stage 2", flashcardDao.getCountByStageInHierarchy(targetIds, 2, timeLimit), "4 hour recall"));
                stageList.add(new MemoryStage("Stage 3", flashcardDao.getCountByStageInHierarchy(targetIds, 3, timeLimit), "24 hour recall"));
                stageList.add(new MemoryStage("Stage 4", flashcardDao.getCountByStageInHierarchy(targetIds, 4, timeLimit), "3 day recall"));
                stageList.add(new MemoryStage("Stage 5", flashcardDao.getCountByStageInHierarchy(targetIds, 5, timeLimit), "7 day recall"));
                stageList.add(new MemoryStage("Stage 6", mastered, "16 day recall"));
            }

            if (isAdded() && getActivity() != null) {
                final int fTotal = total;
                final int fMastered = mastered;
                getActivity().runOnUiThread(() -> {
                    tvTotal.setText(String.valueOf(fTotal));
                    tvMastered.setText(String.valueOf(fMastered));
                    stageAdapter.setStages(stageList);
                });
            }
        }).start();
    }
}
