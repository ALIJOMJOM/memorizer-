package com.ajj.memorizer.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ajj.memorizer.R;
import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.FlashcardDao;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "SRS_REMINDERS";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        FlashcardDao dao = AppDatabase.getDatabase(context).flashcardDao();

        int dueCount = dao.getDueCount(System.currentTimeMillis());

        if (dueCount > 0) {
            showNotification(context, dueCount);
        }

        // Reschedule for the next card
        NotificationScheduler.scheduleReminders(context);

        return Result.success();
    }

    private void showNotification(Context context, int count) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Review Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Memorizer: Time to Review")
                .setContentText("You have " + count + " cards ready for review.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
