package com.example.myscheduleapp20;

public class ScheduleEntry {

    public static final String TYPE_LESSON = "LESSON";
    public static final String TYPE_TASK = "TASK";

    public String id;
    public String dayKey;
    public String type;
    public String title;
    public String details;
    public boolean done;
    public int periodIndex;

    // קונסטרקטור מלא (למערכת שעות)
    public ScheduleEntry(String id,
                         String dayKey,
                         String type,
                         String title,
                         String details,
                         boolean done,
                         int periodIndex) {

        this.id = id;
        this.dayKey = dayKey;
        this.type = type;
        this.title = title;
        this.details = details;
        this.done = done;
        this.periodIndex = periodIndex;
    }

    // קונסטרקטור למשימות רגילות (ללא שעה קבועה)
    public ScheduleEntry(String id,
                         String dayKey,
                         String type,
                         String title,
                         String details,
                         boolean done) {

        this(id, dayKey, type, title, details, done, -1);
    }

    // קונסטרקטור ריק (חשוב ל-loadAll)
    public ScheduleEntry() {
    }
}