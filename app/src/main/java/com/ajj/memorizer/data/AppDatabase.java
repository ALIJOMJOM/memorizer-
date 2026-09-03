package com.ajj.memorizer.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.os.Environment;
import java.io.File;

@Database(entities = {Flashcard.class, Category.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract FlashcardDao flashcardDao();
    public abstract CategoryDao categoryDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Custom path in Documents/Memorizer or similar
                    File dir = new File(Environment.getExternalStorageDirectory(), "Memorizer");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dbFile = new File(dir, "memorizer.db");
                    
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, dbFile.getAbsolutePath())
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
