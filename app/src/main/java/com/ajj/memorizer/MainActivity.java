package com.ajj.memorizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.data.FlashcardDao;
import com.ajj.memorizer.logic.JsonImporter;
import com.ajj.memorizer.ui.HomeFragment;
import com.ajj.memorizer.ui.LibraryFragment;
import com.ajj.memorizer.ui.StatsFragment;
import com.ajj.memorizer.worker.NotificationScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<String> filePickerLauncher;
    private int currentCategoryId = -1;

    public void setCurrentCategoryId(int id) {
        this.currentCategoryId = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment(), "home");
                return true;
            } else if (itemId == R.id.nav_stats) {
                loadFragment(new StatsFragment(), "stats");
                return true;
            } else if (itemId == R.id.nav_library) {
                loadFragment(new LibraryFragment(), "library");
                return true;
            }
            return false;
        });

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home");
        }

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                importJsonFromUri(uri);
            }
        });

        NotificationScheduler.scheduleReminders(this);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showImportOptionsDialog());
    }

    private void showImportOptionsDialog() {
        String[] options = {"Pick JSON File", "Paste JSON Text", "Add New Category", "Add New Flashcard"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Import or Add Content")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickJsonFile();
                    } else if (which == 1) {
                        showPasteJsonDialog();
                    } else if (which == 2) {
                        showAddCategoryDialog();
                    } else if (which == 3) {
                        Intent intent = new Intent(this, AddFlashcardActivity.class);
                        if (currentCategoryId != -1) intent.putExtra("categoryId", currentCategoryId);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void showPasteJsonDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Paste JSON here...");
        input.setGravity(android.view.Gravity.TOP);
        input.setLines(10);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Paste JSON Content")
                .setView(input)
                .setPositiveButton("Import", (dialog, which) -> {
                    String json = input.getText().toString().trim();
                    if (!json.isEmpty()) {
                        performImport(json);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCategoryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Category Name");
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add New Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        new Thread(() -> {
                            Integer parentId = (currentCategoryId == -1) ? null : currentCategoryId;
                            AppDatabase.getDatabase(this).categoryDao().insert(new com.ajj.memorizer.data.Category(name, parentId));
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    public void pickJsonFile() {
        filePickerLauncher.launch("application/json");
    }

    private void importJsonFromUri(android.net.Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            inputStream.close();
            performImport(sb.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performImport(String json) {
        new Thread(() -> {
            try {
                JsonImporter.importHierarchicalJson(AppDatabase.getDatabase(this), json);
                runOnUiThread(() -> Toast.makeText(this, "Import successful!", Toast.LENGTH_SHORT).show());
            } catch (JSONException e) {
                runOnUiThread(() -> Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        Fragment libraryFragment = getSupportFragmentManager().findFragmentByTag("library");
        if (libraryFragment instanceof LibraryFragment) {
            if (((LibraryFragment) libraryFragment).handleBackNavigation()) {
                return;
            }
        }
        super.onBackPressed();
    }
}
