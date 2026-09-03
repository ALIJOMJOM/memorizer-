package com.ajj.memorizer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;

import com.ajj.memorizer.MainActivity;
import com.ajj.memorizer.R;
import com.ajj.memorizer.StudyActivity;
import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.FlashcardDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FlashcardDao flashcardDao;
    private TextView tvDueCount, tvNextRecallTime, tvNextRecallCountdown;
    private Button btnStudy, btnImport;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        flashcardDao = AppDatabase.getDatabase(requireContext()).flashcardDao();
        tvDueCount = view.findViewById(R.id.tv_due_count);
        tvNextRecallTime = view.findViewById(R.id.tv_next_recall_time);
        tvNextRecallCountdown = view.findViewById(R.id.tv_next_recall_countdown);
        btnStudy = view.findViewById(R.id.btn_study);
        btnImport = view.findViewById(R.id.btn_import);

        btnStudy.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StudyActivity.class);
            startActivity(intent);
        });

        btnImport.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).pickJsonFile();
            }
        });

        // Reset context to root when on Home
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentCategoryId(-1);
        }

        observeData();

        return view;
    }

    private void observeData() {
        // Observe due count
        new Thread(() -> {
            while (isAdded()) {
                int count = flashcardDao.getDueCount(System.currentTimeMillis());
                int total = flashcardDao.getTotalCount();
                Long nextTimestamp = flashcardDao.getNextReviewTimestamp(System.currentTimeMillis());
                
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvDueCount.setText(String.valueOf(count));
                        btnStudy.setEnabled(total > 0);
                        updateNextRecallUI(nextTimestamp);
                    });
                }
                try {
                    Thread.sleep(10000); // Update every 10 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void updateNextRecallUI(Long nextTimestamp) {
        if (nextTimestamp == null || nextTimestamp == 0) {
            tvNextRecallTime.setText("No cards scheduled");
            tvNextRecallCountdown.setText("Keep studying to grow your mind");
            stopCountdown();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        tvNextRecallTime.setText(sdf.format(new Date(nextTimestamp)));

        startCountdown(nextTimestamp);
    }

    private void startCountdown(long targetTime) {
        stopCountdown();
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long diff = targetTime - System.currentTimeMillis();
                if (diff <= 0) {
                    tvNextRecallCountdown.setText("Session ready!");
                    observeData(); // Refresh to update count
                    return;
                }

                long hours = diff / (1000 * 60 * 60);
                long minutes = (diff / (1000 * 60)) % 60;
                long seconds = (diff / 1000) % 60;

                String timeLeft = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                tvNextRecallCountdown.setText("Starts in " + timeLeft);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(countdownRunnable);
    }

    private void stopCountdown() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdown();
    }
}
