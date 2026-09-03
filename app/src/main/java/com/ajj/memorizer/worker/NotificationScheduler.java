package com.ajj.memorizer.worker;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.FlashcardDao;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String UNIQUE_WORK_NAME = "SRS_ONE_TIME_REMINDER";

    public static void scheduleReminders(Context context) {
        new Thread(() -> {
            FlashcardDao dao = AppDatabase.getDatabase(context).flashcardDao();
            long currentTime = System.currentTimeMillis();
            Long nextTime = dao.getNextReviewTimestamp(currentTime);

            if (nextTime != null) {
                long delay = nextTime - currentTime;
                if (delay < 0) delay = 0;

                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

                WorkManager.getInstance(context).enqueueUniqueWork(
                        UNIQUE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                );
            }
        }).start();
    }
}
