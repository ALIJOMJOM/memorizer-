package com.ajj.memorizer.ui.model;

import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;
import com.ajj.memorizer.data.Flashcard;

public class LibraryItem {
    public enum Type { FOLDER, FILE }

    private Type type;
    private CategoryDao.CategoryWithStats categoryWithStats;
    private Flashcard flashcard;
    private int positionInList;

    public LibraryItem(CategoryDao.CategoryWithStats categoryWithStats) {
        this.type = Type.FOLDER;
        this.categoryWithStats = categoryWithStats;
    }

    public LibraryItem(Flashcard flashcard, int position) {
        this.type = Type.FILE;
        this.flashcard = flashcard;
        this.positionInList = position;
    }

    public Type getType() { return type; }
    public CategoryDao.CategoryWithStats getCategoryWithStats() { return categoryWithStats; }
    public Flashcard getFlashcard() { return flashcard; }
    public int getPositionInList() { return positionInList; }
}
