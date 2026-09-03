package com.ajj.memorizer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    long insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcards WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    LiveData<List<Flashcard>> getDueCards(long currentTime);

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId AND nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    LiveData<List<Flashcard>> getDueCardsByCategory(long currentTime, int categoryId);

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId ORDER BY id DESC")
    LiveData<List<Flashcard>> getCardsByCategory(int categoryId);

    @Query("SELECT * FROM flashcards ORDER BY id DESC")
    LiveData<List<Flashcard>> getAllCards();

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewTimestamp <= :currentTime")
    int getDueCount(long currentTime);

    @Query("SELECT MIN(nextReviewTimestamp) FROM flashcards WHERE nextReviewTimestamp > :currentTime")
    Long getNextReviewTimestamp(long currentTime);

    @Query("SELECT * FROM flashcards WHERE id = :id")
    Flashcard getCardById(int id);

    @Query("SELECT COUNT(*) FROM flashcards")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM flashcards WHERE intervalStage >= 6")
    int getMasteredCount();

    @Query("SELECT COUNT(*) FROM flashcards WHERE intervalStage = :stage")
    int getCountByStage(int stage);

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId OR categoryId IN (SELECT id FROM categories WHERE parentId = :categoryId) ORDER BY id DESC")
    LiveData<List<Flashcard>> getAllCardsInHierarchy(int categoryId);
}
