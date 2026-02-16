package com.example.myscheduleapp20;

public class ScheduleItem {

    private final String id;
    private final String title;
    private final String displayTime;
    private final String details;
    private final long timeMillis;
    private final int alarmId;

    public ScheduleItem(String id, String title, String displayTime, String details, long timeMillis, int alarmId) {
        this.id = id;
        this.title = title;
        this.displayTime = displayTime;
        this.details = details;
        this.timeMillis = timeMillis;
        this.alarmId = alarmId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDisplayTime() { return displayTime; }
    public String getDetails() { return details; }
    public long getTimeMillis() { return timeMillis; }
    public int getAlarmId() { return alarmId; }

    // פונקציה חדשה: בודקת אם זמן המשימה כבר עבר
    public boolean isPast() {
        return System.currentTimeMillis() > timeMillis;
    }
}