package com.example.myscheduleapp20.model;

import com.google.firebase.firestore.Exclude;

public class ScheduleItem {
    private String id;
    private String userId;
    private String day;
    private String scheduleType;
    private int periodNumber;
    private String subjectName;
    private String startTime;
    private String endTime;
    private String note;
    private int alarmHour;
    private int alarmMinute;
    private long alarmTime;
    private int alarmId;

    public ScheduleItem() {} // חובה עבור Firebase

    public ScheduleItem(String userId, String day, String scheduleType, int periodNumber,
                        String subjectName, String startTime, String endTime, String note,
                        int alarmHour, int alarmMinute, long alarmTime, int alarmId) {
        this.userId = userId;
        this.day = day;
        this.scheduleType = scheduleType;
        this.periodNumber = periodNumber;
        this.subjectName = subjectName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.alarmHour = alarmHour;
        this.alarmMinute = alarmMinute;
        this.alarmTime = alarmTime;
        this.alarmId = alarmId;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDay() { return day; }
    public String getScheduleType() { return scheduleType; }
    public int getPeriodNumber() { return periodNumber; }
    public String getSubjectName() { return subjectName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getNote() { return note != null ? note : ""; }
    public int getAlarmHour() { return alarmHour; }
    public int getAlarmMinute() { return alarmMinute; }
    public long getAlarmTime() { return alarmTime; }
    public int getAlarmId() { return alarmId; }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}