package com.example.myscheduleapp20.model;

public class Task {

    private String id;
    private String title;
    private long alarmTime;
    private int alarmId;
    private boolean done;

    public Task() {
        // חובה ל-Firestore
    }

    public Task(String title, long alarmTime, int alarmId) {
        this.title = title;
        this.alarmTime = alarmTime;
        this.alarmId = alarmId;
        this.done = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public long getAlarmTime() { return alarmTime; }

    public int getAlarmId() { return alarmId; }

    public boolean isDone() { return done; }

    public void setDone(boolean done) { this.done = done; }
}