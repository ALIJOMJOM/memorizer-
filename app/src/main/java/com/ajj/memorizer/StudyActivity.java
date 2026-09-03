package com.ajj.memorizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.data.FlashcardDao;
import com.ajj.memorizer.logic.SRSLogic;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends AppCompatActivity {

    private FlashcardDao flashcardDao;
    private CategoryDao categoryDao;
    private List<Flashcard> dueCards = new ArrayList<>();
    private int currentIndex = 0;
    private int categoryId = -1;
    private boolean isPracticeMode = false;

    private TextView tvQuestion, tvAnswer, tvBreadcrumbs;
    private LinearProgressIndicator progressBar;
    private Button btnReveal;
    private LinearLayout layoutRatings;
    private Button btnHard, btnGood, btnEasy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        categoryId = getIntent().getIntExtra("categoryId", -1);
        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();
        categoryDao = AppDatabase.getDatabase(this).categoryDao();

        tvQuestion = findViewById(R.id.tv_study_question);
        tvAnswer = findViewById(R.id.tv_study_answer);
        tvBreadcrumbs = findViewById(R.id.tv_breadcrumbs);
        progressBar = findViewById(R.id.progress_study);
        btnReveal = findViewById(R.id.btn_reveal);
        layoutRatings = findViewById(R.id.layout_ratings);
        btnHard = findViewById(R.id.btn_hard);
        btnGood = findViewById(R.id.btn_good);
        btnEasy = findViewById(R.id.btn_easy);

        showModeSelectionDialog();

        btnReveal.setOnClickListener(v -> showAnswer());
        btnHard.setOnClickListener(v -> rateCard(SRSLogic.Rating.HARD));
        btnGood.setOnClickListener(v -> rateCard(SRSLogic.Rating.GOOD));
        btnEasy.setOnClickListener(v -> rateCard(SRSLogic.Rating.EASY));
    }

    private void showModeSelectionDialog() {
        String[] modes = {
            "Real Review (Due cards only)",
            "Play Now (Study ahead & Update schedule)",
            "Practice Mode (Play without removing next)",
            "Restart Current Session"
        };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Study Mode")
                .setCancelable(true)
                .setItems(modes, (dialog, which) -> {
                    if (which == 0) {
                        isPracticeMode = false;
                        loadDueOnly();
                    } else if (which == 1) {
                        isPracticeMode = false;
                        loadAllCards();
                    } else if (which == 2) {
                        isPracticeMode = true;
                        loadAllCards();
                    } else {
                        // Restart progress and show dialog again
                        new Thread(() -> {
                            saveProgress(0);
                            currentIndex = 0;
                            runOnUiThread(this::showModeSelectionDialog);
                        }).start();
                    }
                })
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void loadDueOnly() {
        if (categoryId == -1) {
            flashcardDao.getDueCards(System.currentTimeMillis()).observe(this, this::setupSession);
        } else {
            flashcardDao.getDueCardsByCategory(System.currentTimeMillis(), categoryId).observe(this, this::setupSession);
        }
        if (categoryId != -1) updateBreadcrumbs();
    }

    private void loadAllCards() {
        if (categoryId == -1) {
            flashcardDao.getAllCards().observe(this, this::setupSession);
        } else {
            flashcardDao.getAllCardsInHierarchy(categoryId).observe(this, this::setupSession);
        }
        if (categoryId != -1) updateBreadcrumbs();
    }

    private void setupSession(List<Flashcard> cards) {
        if (cards != null && !cards.isEmpty() && dueCards.isEmpty()) {
            dueCards.addAll(cards);
            if (!isPracticeMode && categoryId != -1) {
                new Thread(() -> {
                    Category c = categoryDao.getCategoryById(categoryId);
                    if (c != null && c.getLastStudyIndex() < dueCards.size()) {
                        currentIndex = c.getLastStudyIndex();
                    }
                    runOnUiThread(this::displayCurrentCard);
                }).start();
            } else {
                displayCurrentCard();
            }
        } else if (dueCards.isEmpty()) {
            Toast.makeText(this, "No cards available!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateBreadcrumbs() {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            Category current = categoryDao.getCategoryById(categoryId);
            while (current != null) {
                if (sb.length() > 0) sb.insert(0, " > ");
                sb.insert(0, current.getName());
                if (current.getParentId() != null) {
                    current = categoryDao.getCategoryById(current.getParentId());
                } else {
                    current = null;
                }
            }
            String path = sb.toString();
            runOnUiThread(() -> tvBreadcrumbs.setText(path));
        }).start();
    }

    private void displayCurrentCard() {
        if (currentIndex < dueCards.size()) {
            Flashcard card = dueCards.get(currentIndex);
            String question = (currentIndex + 1) + ". " + card.getQuestion();
            tvQuestion.setText(question);
            tvAnswer.setText(card.getAnswer());
            tvAnswer.setVisibility(View.INVISIBLE);
            btnReveal.setVisibility(View.VISIBLE);
            layoutRatings.setVisibility(View.GONE);

            progressBar.setProgress((int) (((float) (currentIndex + 1) / dueCards.size()) * 100));
        } else {
            Toast.makeText(this, "Study session finished!", Toast.LENGTH_SHORT).show();
            // Reset index on finish
            saveProgress(0);
            finish();
        }
    }

    private void showAnswer() {
        tvAnswer.setVisibility(View.VISIBLE);
        btnReveal.setVisibility(View.GONE);
        layoutRatings.setVisibility(View.VISIBLE);

        Flashcard card = dueCards.get(currentIndex);
        btnHard.setText("Hard\n(" + SRSLogic.getNextIntervalString(card, SRSLogic.Rating.HARD) + ")");
        btnGood.setText("Good\n(" + SRSLogic.getNextIntervalString(card, SRSLogic.Rating.GOOD) + ")");
        btnEasy.setText("Easy\n(" + SRSLogic.getNextIntervalString(card, SRSLogic.Rating.EASY) + ")");
    }

    private void rateCard(SRSLogic.Rating rating) {
        Flashcard card = dueCards.get(currentIndex);
        
        if (!isPracticeMode) {
            SRSLogic.updateCard(card, rating);
            new Thread(() -> {
                flashcardDao.update(card);
                saveProgress(currentIndex + 1);
            }).start();
        }

        runOnUiThread(() -> {
            currentIndex++;
            displayCurrentCard();
        });
    }

    private void saveProgress(int index) {
        if (categoryId != -1) {
            new Thread(() -> {
                Category c = categoryDao.getCategoryById(categoryId);
                if (c != null) {
                    c.setLastStudyIndex(index);
                    categoryDao.update(c);
                }
            }).start();
        }
    }
}
