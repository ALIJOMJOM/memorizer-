package com.ajj.memorizer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories WHERE parentId IS NULL")
    LiveData<List<Category>> getRootCategories();

    @Query("SELECT * FROM categories WHERE parentId = :parentId")
    LiveData<List<Category>> getSubCategories(int parentId);

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(int id);

    @Query("WITH RECURSIVE descendants AS (" +
           "SELECT id FROM categories WHERE id = :categoryId " +
           "UNION ALL " +
           "SELECT c.id FROM categories c JOIN descendants d ON c.parentId = d.id" +
           ") SELECT id FROM descendants")
    List<Integer> getDescendantIds(int categoryId);

    @Query("SELECT * FROM categories WHERE name = :name AND (parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL)) LIMIT 1")
    Category getCategoryByName(String name, Integer parentId);

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE parentId = :parentId")
    List<Category> getSubCategoriesSync(int parentId);

    @Query("SELECT * FROM categories WHERE parentId IS NULL")
    List<Category> getRootCategoriesSync();

    @Query("SELECT c.*, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id) as cardCount, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id AND nextReviewTimestamp <= :currentTime) as dueCount, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id AND intervalStage >= 6) as masteredCount FROM categories c WHERE c.parentId IS NULL")
    LiveData<List<CategoryWithStats>> getRootCategoriesWithStats(long currentTime);

    @Query("SELECT c.*, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id) as cardCount, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id AND nextReviewTimestamp <= :currentTime) as dueCount, (SELECT COUNT(*) FROM flashcards WHERE categoryId = c.id AND intervalStage >= 6) as masteredCount FROM categories c WHERE c.parentId = :parentId")
    LiveData<List<CategoryWithStats>> getSubCategoriesWithStats(int parentId, long currentTime);

    class CategoryWithStats {
        @Embedded
        public Category category;
        public int cardCount;
        public int dueCount;
        public int masteredCount;
    }
}
