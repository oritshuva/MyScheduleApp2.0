package com.example.myscheduleapp20.model;

public class Period {

    private int periodNumber;
    private String subject;
    private String startTime;
    private String endTime;

    public Period(int periodNumber, String subject, String startTime, String endTime) {
        this.periodNumber = periodNumber;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getPeriodNumber() { return periodNumber; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStartTime() { return startTime; }

    public String getEndTime() { return endTime; }

    public String getDisplayTitle() {
        return "שעה " + periodNumber + " - " + (subject == null || subject.isEmpty() ? "ריק" : subject);
    }

    public String getDisplayTime() {
        return startTime + " - " + endTime;
    }
}