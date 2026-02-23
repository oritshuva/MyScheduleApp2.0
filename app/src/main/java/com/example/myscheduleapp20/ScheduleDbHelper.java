package com.example.myscheduleapp20;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScheduleDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "schedule_local.db";
    private static final int DB_VERSION = 1;

    public ScheduleDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + ScheduleContract.TaskEntry.TABLE_NAME + " ("
                + ScheduleContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScheduleContract.TaskEntry.COLUMN_FIREBASE_DOC_ID + " TEXT UNIQUE, "
                + ScheduleContract.TaskEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + ScheduleContract.TaskEntry.COLUMN_DISPLAY_TIME + " TEXT, "
                + ScheduleContract.TaskEntry.COLUMN_DETAILS + " TEXT, "
                + ScheduleContract.TaskEntry.COLUMN_TIME_MILLIS + " INTEGER, "
                + ScheduleContract.TaskEntry.COLUMN_ALARM_ID + " INTEGER, "
                + ScheduleContract.TaskEntry.COLUMN_SCHEDULE_TYPE + " TEXT"
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // כרגע לא צריך
    }
}
