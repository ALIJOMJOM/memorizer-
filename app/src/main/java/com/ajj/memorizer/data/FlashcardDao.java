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

    @Insert
    void insertAll(List<Flashcard> flashcards);

    @Query("SELECT * FROM flashcards")
    List<Flashcard> getAllCardsSync();

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId")
    List<Flashcard> getCardsByCategorySync(int categoryId);

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
    @Query("SELECT COUNT(*) FROM flashcards WHERE categoryId = :categoryId OR categoryId IN (SELECT id FROM categories WHERE parentId = :categoryId)")
    int getCountInHierarchySync(int categoryId);
    @Query("SELECT COUNT(*) FROM flashcards WHERE (categoryId IN (:ids)) AND nextReviewTimestamp <= :time")
    int getDueCountInHierarchy(List<Integer> ids, long time);

    @Query("SELECT COUNT(*) FROM flashcards WHERE (categoryId IN (:ids)) AND nextReviewTimestamp <= :time")
    int getTotalCountInHierarchy(List<Integer> ids, long time);

    @Query("SELECT COUNT(*) FROM flashcards WHERE (categoryId IN (:ids)) AND intervalStage >= 6 AND nextReviewTimestamp <= :time")
    int getMasteredCountInHierarchy(List<Integer> ids, long time);

    @Query("SELECT COUNT(*) FROM flashcards WHERE (categoryId IN (:ids)) AND intervalStage = :stage AND nextReviewTimestamp <= :time")
    int getCountByStageInHierarchy(List<Integer> ids, int stage, long time);
}
