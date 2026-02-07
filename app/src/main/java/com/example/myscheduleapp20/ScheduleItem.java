package com.example.myscheduleapp20;

public class ScheduleItem {

    private final String id;          // Firestore docId
    private final String title;
    private final String displayTime; // לתצוגה: "dd/MM/yyyy HH:mm"
    private final String details;
    private final long time;          // number: millis (למיון/תזכורות)

    public ScheduleItem(String id, String title, String displayTime, String details, long time) {
        this.id = id;
        this.title = title;
        this.displayTime = displayTime;
        this.details = details;
        this.time = time;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDisplayTime() { return displayTime; }
    public String getDetails() { return details; }
    public long getTime() { return time; }
}
