package com.ajj.memorizer.logic;

import com.ajj.memorizer.data.Flashcard;
import java.util.concurrent.TimeUnit;

public class SRSLogic {

    public enum Rating {
        HARD, GOOD, EASY
    }

    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long FOUR_HOURS = TimeUnit.HOURS.toMillis(4);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long THREE_DAYS = TimeUnit.DAYS.toMillis(3);
    private static final long SEVEN_DAYS = TimeUnit.DAYS.toMillis(7);
    private static final long SIXTEEN_DAYS = TimeUnit.DAYS.toMillis(16);

    /**
     * Updates the flashcard's SRS data based on user rating.
     */
    public static void updateCard(Flashcard card, Rating rating) {
        long currentTime = System.currentTimeMillis();
        card.setLastReviewedTimestamp(currentTime);

        int currentStage = card.getIntervalStage();
        int nextStage;

        switch (rating) {
            case HARD:
                // HARD Track: 1h (1) -> 4h (2) -> 24h (3)
                if (currentStage == 0 || currentStage >= 3) {
                    nextStage = 1; // Start Hard track
                } else {
                    nextStage = Math.min(3, currentStage + 1);
                }
                break;

            case GOOD:
                // GOOD Track: 24h (3) -> 3d (4) -> 7d (5)
                if (currentStage < 3) {
                    nextStage = 3; // Start Good track
                } else {
                    nextStage = Math.min(5, currentStage + 1);
                }
                break;

            case EASY:
                // EASY Track: 3d (4) -> 7d (5) -> 16d (6)
                if (currentStage < 4) {
                    nextStage = 4; // Start Easy track
                } else {
                    nextStage = Math.min(6, currentStage + 1);
                }
                break;

            default:
                nextStage = 1;
        }

        card.setIntervalStage(nextStage);
        card.setNextReviewTimestamp(currentTime + getIntervalForStage(nextStage));
    }

    public static String getNextIntervalString(Flashcard card, Rating rating) {
        int currentStage = card.getIntervalStage();
        int nextStage;

        switch (rating) {
            case HARD:
                if (currentStage == 0 || currentStage >= 3) nextStage = 1;
                else nextStage = Math.min(3, currentStage + 1);
                break;
            case GOOD:
                if (currentStage < 3) nextStage = 3;
                else nextStage = Math.min(5, currentStage + 1);
                break;
            case EASY:
                if (currentStage < 4) nextStage = 4;
                else nextStage = Math.min(6, currentStage + 1);
                break;
            default:
                nextStage = 1;
        }

        long interval = getIntervalForStage(nextStage);
        if (interval < ONE_DAY) {
            return (interval / ONE_HOUR) + "h";
        } else {
            return (interval / ONE_DAY) + "d";
        }
    }

    private static long getIntervalForStage(int stage) {
        switch (stage) {
            case 1: return ONE_HOUR;
            case 2: return FOUR_HOURS;
            case 3: return ONE_DAY;
            case 4: return THREE_DAYS;
            case 5: return SEVEN_DAYS;
            case 6: return SIXTEEN_DAYS;
            default: return ONE_HOUR;
        }
    }
}
