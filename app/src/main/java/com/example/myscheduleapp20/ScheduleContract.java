package com.example.myscheduleapp20;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ScheduleContract {

    private ScheduleContract() {}

    public static final String AUTHORITY = "com.example.myscheduleapp20.scheduleprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASKS = "tasks";

    public static final class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        public static final String COLUMN_FIREBASE_DOC_ID = "firebase_doc_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DISPLAY_TIME = "display_time";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TIME_MILLIS = "time_millis";
        public static final String COLUMN_ALARM_ID = "alarm_id";
        public static final String COLUMN_SCHEDULE_TYPE = "schedule_type";
    }
}
