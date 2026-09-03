package com.ajj.memorizer.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "flashcards",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "categoryId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("categoryId")}
)
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;
    private int categoryId;

    private int intervalStage; // 0, 1, 2, 3, 4, 5+
    private long nextReviewTimestamp;
    private long lastReviewedTimestamp;
    private float easeFactor;

    public Flashcard(String question, String answer, int categoryId) {
        this.question = question;
        this.answer = answer;
        this.categoryId = categoryId;
        this.intervalStage = 0;
        this.easeFactor = 2.5f; 
        this.nextReviewTimestamp = System.currentTimeMillis();
        this.lastReviewedTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public int getIntervalStage() { return intervalStage; }
    public void setIntervalStage(int intervalStage) { this.intervalStage = intervalStage; }

    public long getNextReviewTimestamp() { return nextReviewTimestamp; }
    public void setNextReviewTimestamp(long nextReviewTimestamp) { this.nextReviewTimestamp = nextReviewTimestamp; }

    public long getLastReviewedTimestamp() { return lastReviewedTimestamp; }
    public void setLastReviewedTimestamp(long lastReviewedTimestamp) { this.lastReviewedTimestamp = lastReviewedTimestamp; }

    public float getEaseFactor() { return easeFactor; }
    public void setEaseFactor(float easeFactor) { this.easeFactor = easeFactor; }
}
