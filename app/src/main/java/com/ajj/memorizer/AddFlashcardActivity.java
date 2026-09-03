package com.ajj.memorizer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.data.FlashcardDao;

public class AddFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer;
    private FlashcardDao flashcardDao;
    private int cardId = -1;
    private int categoryId = -1;
    private Flashcard existingCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();

        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        Button btnSave = findViewById(R.id.btn_save);

        categoryId = getIntent().getIntExtra("categoryId", -1);

        if (getIntent().hasExtra("card_id")) {
            cardId = getIntent().getIntExtra("card_id", -1);
            loadCardData();
        }

        btnSave.setOnClickListener(v -> {
            String q = etQuestion.getText().toString().trim();
            String a = etAnswer.getText().toString().trim();

            if (q.isEmpty() || a.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                int finalCategoryId = categoryId;
                if (finalCategoryId == -1) {
                    // Default to 'General' if no category is contextually active
                    com.ajj.memorizer.data.Category cat = AppDatabase.getDatabase(this).categoryDao().getCategoryByName("General", null);
                    if (cat == null) {
                        finalCategoryId = (int) AppDatabase.getDatabase(this).categoryDao().insert(new com.ajj.memorizer.data.Category("General", null));
                    } else {
                        finalCategoryId = cat.getId();
                    }
                }

                if (existingCard == null) {
                    flashcardDao.insert(new com.ajj.memorizer.data.Flashcard(q, a, finalCategoryId));
                } else {
                    existingCard.setQuestion(q);
                    existingCard.setAnswer(a);
                    existingCard.setCategoryId(finalCategoryId);
                    flashcardDao.update(existingCard);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    private void loadCardData() {
        new Thread(() -> {
            existingCard = flashcardDao.getCardById(cardId);
            runOnUiThread(() -> {
                if (existingCard != null) {
                    etQuestion.setText(existingCard.getQuestion());
                    etAnswer.setText(existingCard.getAnswer());
                    categoryId = existingCard.getCategoryId();
                    setTitle("Edit Flashcard");
                }
            });
        }).start();
    }
}
