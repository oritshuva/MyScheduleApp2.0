package com.example.myscheduleapp20;

public class ScheduleEntry {

    public static final String TYPE_LESSON = "lesson";
    public static final String TYPE_TASK = "task";

    public String id;
    public String dayKey;
    public String type;

    public String title;
    public String details;

    public boolean done;

    public ScheduleEntry() {
    }

    public ScheduleEntry(String id,
                         String dayKey,
                         String type,
                         String title,
                         String details,
                         boolean done) {

        this.id = id;
        this.dayKey = dayKey;
        this.type = type;
        this.title = title;
        this.details = details;
        this.done = done;
    }
}