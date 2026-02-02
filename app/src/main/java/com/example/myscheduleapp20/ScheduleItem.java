package com.example.myscheduleapp20;

public class ScheduleItem {

    private final String title;
    private final String time;
    private final String details;

    public ScheduleItem(String title, String time, String details) {
        this.title = title;
        this.time = time;
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDetails() {
        return details;
    }
}
