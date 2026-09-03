package com.ajj.memorizer.ui.model;

public class MemoryStage {
    private String title;
    private int cardCount;
    private String interval;

    public MemoryStage(String title, int cardCount, String interval) {
        this.title = title;
        this.cardCount = cardCount;
        this.interval = interval;
    }

    public String getTitle() { return title; }
    public int getCardCount() { return cardCount; }
    public String getInterval() { return interval; }
}
