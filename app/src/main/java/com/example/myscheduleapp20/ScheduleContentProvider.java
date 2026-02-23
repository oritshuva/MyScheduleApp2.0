package com.example.myscheduleapp20;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ScheduleContentProvider extends ContentProvider {

    private static final int TASKS = 100;
    private static final int TASK_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ScheduleDbHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ScheduleContract.AUTHORITY, ScheduleContract.PATH_TASKS, TASKS);
        matcher.addURI(ScheduleContract.AUTHORITY, ScheduleContract.PATH_TASKS + "/#", TASK_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new ScheduleDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                cursor = db.query(
                        ScheduleContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case TASK_ID:
                String id = String.valueOf(ContentUris.parseId(uri));
                cursor = db.query(
                        ScheduleContract.TaskEntry.TABLE_NAME,
                        projection,
                        ScheduleContract.TaskEntry._ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                return "vnd.android.cursor.dir/" + ScheduleContract.AUTHORITY + "." + ScheduleContract.PATH_TASKS;
            case TASK_ID:
                return "vnd.android.cursor.item/" + ScheduleContract.AUTHORITY + "." + ScheduleContract.PATH_TASKS;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != TASKS) {
            throw new IllegalArgumentException("Insert not supported for URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(ScheduleContract.TaskEntry.TABLE_NAME, null, values);

        if (id == -1) {
            throw new IllegalStateException("Insert failed: " + uri);
        }

        Uri returnUri = ContentUris.withAppendedId(ScheduleContract.TaskEntry.CONTENT_URI, id);
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows;

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                rows = db.update(ScheduleContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TASK_ID:
                String id = String.valueOf(ContentUris.parseId(uri));
                rows = db.update(
                        ScheduleContract.TaskEntry.TABLE_NAME,
                        values,
                        ScheduleContract.TaskEntry._ID + "=?",
                        new String[]{id}
                );
                break;

            default:
                throw new IllegalArgumentException("Update not supported for URI: " + uri);
        }

        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows;

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                rows = db.delete(ScheduleContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TASK_ID:
                String id = String.valueOf(ContentUris.parseId(uri));
                rows = db.delete(
                        ScheduleContract.TaskEntry.TABLE_NAME,
                        ScheduleContract.TaskEntry._ID + "=?",
                        new String[]{id}
                );
                break;

            default:
                throw new IllegalArgumentException("Delete not supported for URI: " + uri);
        }

        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}
