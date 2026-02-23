package com.example.myscheduleapp20.model;

import com.google.firebase.firestore.Exclude;

public class ScheduleItem {
    private String id;
    private String title;
    private String displayTime;
    private String details;
    private long time;
    private int alarmId;
    private String scheduleType;

    // חובה עבור Firebase
    public ScheduleItem() {}

    public ScheduleItem(String title, String displayTime, String details, long time, int alarmId, String scheduleType) {
        this.title = title;
        this.displayTime = displayTime;
        this.details = details;
        this.time = time;
        this.alarmId = alarmId;
        this.scheduleType = scheduleType;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDisplayTime() { return displayTime; }
    public void setDisplayTime(String displayTime) { this.displayTime = displayTime; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    // לשימוש נוח אם חלק מהקוד קורא בשם הזה
    @Exclude
    public long getTimeMillis() { return time; }
    public void setTimeMillis(long time) { this.time = time; }

    public int getAlarmId() { return alarmId; }
    public void setAlarmId(int alarmId) { this.alarmId = alarmId; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    @Exclude
    public boolean isPast() {
        return System.currentTimeMillis() > this.time;
    }
}
