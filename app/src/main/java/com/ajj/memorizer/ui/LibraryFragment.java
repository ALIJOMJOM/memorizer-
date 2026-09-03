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
import com.ajj.memorizer.data.CategoryRepository;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LibraryFragment extends Fragment {

    private CategoryDao categoryDao;
    private FlashcardDao flashcardDao;
    private RecyclerView rvCategories;
    private TextView tvBreadcrumbs;
    private View btnStudyFolder;
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
        btnStudyFolder = view.findViewById(R.id.btn_study_folder);

        btnStudyFolder.setOnClickListener(v -> {
            Integer currentId = navigationStack.isEmpty() ? null : navigationStack.peek().getId();
            if (currentId != null) {
                Intent intent = new Intent(getActivity(), StudyActivity.class);
                intent.putExtra("categoryId", currentId);
                startActivity(intent);
            }
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibraryAdapter(new ArrayList<>(), new LibraryAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(Category category) {
                navigationStack.push(category);
                loadContent();
            }

            @Override
            public void onFolderOptions(Category category, View anchor) {
                showFolderOptionsMenu(category, anchor);
            }

            @Override
            public void onFileOptions(Flashcard flashcard, View anchor) {
                showFileOptionsMenu(flashcard, anchor);
            }
        });
        rvCategories.setAdapter(adapter);

        loadContent();

        return view;
    }

    private void refreshUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadContent);
        }
    }

    private void loadContent() {
        Integer parentId = navigationStack.isEmpty() ? null : navigationStack.peek().getId();
        
        // Notify MainActivity of current category for FAB
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentCategoryId(parentId != null ? parentId : -1);
        }

        // Update breadcrumbs - MUST be on UI thread
        StringBuilder sb = new StringBuilder("Library");
        for (Category c : navigationStack) {
            sb.append(" > ").append(c.getName());
        }
        tvBreadcrumbs.setText(sb.toString());
        btnStudyFolder.setVisibility(parentId == null ? View.GONE : View.VISIBLE);

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

    private void showFileOptionsMenu(Flashcard flashcard, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Move");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Edit")) {
                Intent intent = new Intent(getActivity(), AddFlashcardActivity.class);
                intent.putExtra("card_id", flashcard.getId());
                intent.putExtra("categoryId", flashcard.getCategoryId());
                startActivity(intent);
            } else if (title.equals("Move")) {
                showMoveFileDialog(flashcard);
            } else if (title.equals("Delete")) {
                showFileDeleteConfirmation(flashcard);
            }
            return true;
        });
        popup.show();
    }

    private void showFileDeleteConfirmation(Flashcard flashcard) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Flashcard")
                .setMessage("Are you sure you want to delete this card?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        flashcardDao.delete(flashcard);
                        refreshUI();
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMoveFileDialog(Flashcard flashcard) {
        new Thread(() -> {
            List<Category> all = categoryDao.getAllCategoriesSync();
            List<String> names = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            
            for (Category c : all) {
                names.add(c.getName());
                ids.add(c.getId());
            }

            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Move Flashcard to...")
                        .setItems(names.toArray(new String[0]), (dialog, which) -> {
                            Integer targetId = ids.get(which);
                            new Thread(() -> {
                                flashcard.setCategoryId(targetId);
                                flashcardDao.update(flashcard);
                                refreshUI();
                            }).start();
                        })
                        .show();
            });
        }).start();
    }

    private void showFolderOptionsMenu(Category category, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Rename");
        popup.getMenu().add("Move");
        popup.getMenu().add("Copy");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Rename")) {
                showRenameDialog(category);
            } else if (title.equals("Move")) {
                showMoveDialog(category);
            } else if (title.equals("Copy")) {
                performCopy(category);
            } else if (title.equals("Delete")) {
                showDeleteConfirmation(category);
            }
            return true;
        });
        popup.show();
    }

    private void showRenameDialog(Category category) {
        EditText input = new EditText(requireContext());
        input.setText(category.getName());
        new AlertDialog.Builder(requireContext())
                .setTitle("Rename Category")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        category.setName(newName);
                        new Thread(() -> {
                            categoryDao.update(category);
                            refreshUI();
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(Category category) {
        new Thread(() -> {
            int count = flashcardDao.getCountInHierarchySync(category.getId());
            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Folder")
                        .setMessage("Are you sure? This will delete '" + category.getName() + "' and its " + count + " flashcards permanently.")
                        .setPositiveButton("Delete Everything", (dialog, which) -> {
                            new Thread(() -> {
                            new CategoryRepository(AppDatabase.getDatabase(requireContext())).deleteRecursive(category);
                            refreshUI();
                        }).start();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }).start();
    }

    private void showMoveDialog(Category category) {
        new Thread(() -> {
            List<Category> all = categoryDao.getAllCategoriesSync();
            List<String> names = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            names.add("Root (No Parent)");
            ids.add(null);

            for (Category c : all) {
                if (c.getId() != category.getId()) {
                    names.add(c.getName());
                    ids.add(c.getId());
                }
            }

            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Move to...")
                        .setItems(names.toArray(new String[0]), (dialog, which) -> {
                            Integer targetId = ids.get(which);
                            new Thread(() -> {
                                new CategoryRepository(AppDatabase.getDatabase(requireContext())).move(category, targetId);
                                refreshUI();
                            }).start();
                        })
                        .show();
            });
        }).start();
    }

    private void performCopy(Category category) {
        Toast.makeText(getContext(), "Copying...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            new CategoryRepository(AppDatabase.getDatabase(requireContext())).copyRecursive(category, category.getParentId());
            refreshUI();
        }).start();
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
