package com.example.myscheduleapp20;

public class ScheduleItem {

    private final String id;          // docId ב-Firestore
    private final String title;
    private final String displayTime; // מחרוזת לתצוגה
    private final String details;
    private final long timeMillis;    // מספר (millis) למיון
    private final int alarmId;        // מספר קבוע לתזכורת

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
}
