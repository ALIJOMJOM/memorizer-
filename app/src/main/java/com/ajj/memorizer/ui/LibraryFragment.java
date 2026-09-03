package com.ajj.memorizer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajj.memorizer.MainActivity;
import com.ajj.memorizer.R;
import com.ajj.memorizer.StudyActivity;
import com.ajj.memorizer.AddFlashcardActivity;
import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.data.FlashcardDao;
import com.ajj.memorizer.ui.model.LibraryItem;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LibraryFragment extends Fragment {

    private CategoryDao categoryDao;
    private FlashcardDao flashcardDao;
    private RecyclerView rvCategories;
    private TextView tvBreadcrumbs;
    private LibraryAdapter adapter;
    private Stack<Category> navigationStack = new Stack<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        categoryDao = db.categoryDao();
        flashcardDao = db.flashcardDao();
        
        rvCategories = view.findViewById(R.id.rv_categories);
        tvBreadcrumbs = view.findViewById(R.id.tv_library_breadcrumbs);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibraryAdapter(new ArrayList<>(), new LibraryAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(Category category) {
                navigationStack.push(category);
                loadContent();
            }

            @Override
            public void onFileEdit(Flashcard flashcard) {
                Intent intent = new Intent(getActivity(), AddFlashcardActivity.class);
                intent.putExtra("card_id", flashcard.getId());
                intent.putExtra("categoryId", flashcard.getCategoryId());
                startActivity(intent);
            }

            @Override
            public void onFileDelete(Flashcard flashcard) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Flashcard")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new Thread(() -> flashcardDao.delete(flashcard)).start();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rvCategories.setAdapter(adapter);

        loadContent();

        return view;
    }

    private void loadContent() {
        Integer parentId = navigationStack.isEmpty() ? null : navigationStack.peek().getId();
        
        // Notify MainActivity of current category for FAB
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentCategoryId(parentId != null ? parentId : -1);
        }

        // Update breadcrumbs
        StringBuilder sb = new StringBuilder("Library");
        for (Category c : navigationStack) {
            sb.append(" > ").append(c.getName());
        }
        tvBreadcrumbs.setText(sb.toString());

        long currentTime = System.currentTimeMillis();
        
        // Observe both subcategories and cards
        LiveData<List<CategoryDao.CategoryWithStats>> categoriesLive;
        if (parentId == null) {
            categoriesLive = categoryDao.getRootCategoriesWithStats(currentTime);
        } else {
            categoriesLive = categoryDao.getSubCategoriesWithStats(parentId, currentTime);
        }

        categoriesLive.observe(getViewLifecycleOwner(), categories -> {
            if (parentId == null) {
                updateItems(categories, new ArrayList<>());
            } else {
                flashcardDao.getCardsByCategory(parentId).observe(getViewLifecycleOwner(), cards -> {
                    updateItems(categories, cards);
                });
            }
        });
    }

    private void updateItems(List<CategoryDao.CategoryWithStats> categories, List<Flashcard> cards) {
        List<LibraryItem> items = new ArrayList<>();
        if (categories != null) {
            for (CategoryDao.CategoryWithStats c : categories) {
                items.add(new LibraryItem(c));
            }
        }
        if (cards != null) {
            for (int i = 0; i < cards.size(); i++) {
                items.add(new LibraryItem(cards.get(i), i));
            }
        }
        adapter.setItems(items);
    }

    public boolean handleBackNavigation() {
        if (!navigationStack.isEmpty()) {
            navigationStack.pop();
            loadContent();
            return true;
        }
        return false;
    }
}
