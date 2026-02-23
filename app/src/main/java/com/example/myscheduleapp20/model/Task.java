package com.example.myscheduleapp20.model;

public class Task {
    private String title;
    private String details;
    private long timeInMillis;

    public Task(String title, String details, long timeInMillis) {
        this.title = title;
        this.details = details;
        this.timeInMillis = timeInMillis;
    }

    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public long getTimeInMillis() { return timeInMillis; }

    // פונקציה לבדוק אם הזמן עבר כדי לצבוע בירוק
    public boolean isPast() {
        return System.currentTimeMillis() > timeInMillis;
    }
}