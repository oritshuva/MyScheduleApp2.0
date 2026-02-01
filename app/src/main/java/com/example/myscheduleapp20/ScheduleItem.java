package com.example.myscheduleapp20;

public class ScheduleItem {

    private final String title;
    private final String time;

    public ScheduleItem(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}
