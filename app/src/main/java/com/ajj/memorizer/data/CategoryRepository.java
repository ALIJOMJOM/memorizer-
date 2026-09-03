package com.ajj.memorizer.data;

import java.util.List;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final FlashcardDao flashcardDao;

    public CategoryRepository(AppDatabase db) {
        this.categoryDao = db.categoryDao();
        this.flashcardDao = db.flashcardDao();
    }

    public void deleteRecursive(Category category) {
        // Delete subcategories recursively
        List<Category> subCategories = categoryDao.getSubCategoriesSync(category.getId());
        for (Category sub : subCategories) {
            deleteRecursive(sub);
        }
        // Room cascade should handle flashcards if setup, but let's be explicit if needed.
        // Actually, our entity has CASCADE on flashcards.
        categoryDao.delete(category);
    }

    public void copyRecursive(Category category, Integer targetParentId) {
        // 1. Create a copy of the category
        Category copy = new Category(category.getName() + " (Copy)", targetParentId);
        int newId = (int) categoryDao.insert(copy);

        // 2. Copy all flashcards
        List<Flashcard> cards = flashcardDao.getCardsByCategorySync(category.getId());
        for (Flashcard card : cards) {
            Flashcard cardCopy = new Flashcard(card.getQuestion(), card.getAnswer(), newId);
            cardCopy.setIntervalStage(card.getIntervalStage());
            cardCopy.setEaseFactor(card.getEaseFactor());
            cardCopy.setNextReviewTimestamp(card.getNextReviewTimestamp());
            flashcardDao.insert(cardCopy);
        }

        // 3. Copy subcategories recursively
        List<Category> subCategories = categoryDao.getSubCategoriesSync(category.getId());
        for (Category sub : subCategories) {
            copyRecursiveInternal(sub, newId);
        }
    }

    private void copyRecursiveInternal(Category category, Integer targetParentId) {
        Category copy = new Category(category.getName(), targetParentId);
        int newId = (int) categoryDao.insert(copy);

        List<Flashcard> cards = flashcardDao.getCardsByCategorySync(category.getId());
        for (Flashcard card : cards) {
            Flashcard cardCopy = new Flashcard(card.getQuestion(), card.getAnswer(), newId);
            cardCopy.setIntervalStage(card.getIntervalStage());
            cardCopy.setEaseFactor(card.getEaseFactor());
            cardCopy.setNextReviewTimestamp(card.getNextReviewTimestamp());
            flashcardDao.insert(cardCopy);
        }

        List<Category> subCategories = categoryDao.getSubCategoriesSync(category.getId());
        for (Category sub : subCategories) {
            copyRecursiveInternal(sub, newId);
        }
    }

    public void move(Category category, Integer newParentId) {
        category.setParentId(newParentId);
        categoryDao.update(category);
    }
}
