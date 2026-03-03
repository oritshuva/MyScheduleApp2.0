package com.example.myscheduleapp20;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeeklyScheduleStore {

    private static final String PREF_NAME = "weekly_schedule_pref";
    private static final String KEY_DATA = "weekly_data";

    private final SharedPreferences prefs;

    public WeeklyScheduleStore(Context context) {
        prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveAll(List<ScheduleEntry> list) {
        try {
            JSONArray array = new JSONArray();

            for (ScheduleEntry entry : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", entry.id);
                obj.put("dayKey", entry.dayKey);
                obj.put("type", entry.type);
                obj.put("title", entry.title);
                obj.put("details", entry.details);
                obj.put("done", entry.done);
                array.put(obj);
            }

            prefs.edit()
                    .putString(KEY_DATA, array.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ScheduleEntry> loadAll() {

        List<ScheduleEntry> list = new ArrayList<>();

        try {
            String json =
                    prefs.getString(KEY_DATA, null);

            if (json == null) return list;

            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                ScheduleEntry entry = new ScheduleEntry(
                        obj.getString("id"),
                        obj.getString("dayKey"),
                        obj.getString("type"),
                        obj.getString("title"),
                        obj.getString("details"),
                        obj.getBoolean("done"),
                        obj.optInt("periodIndex", -1)
                );

                entry.id = obj.getString("id");
                entry.dayKey = obj.getString("dayKey");
                entry.type = obj.getString("type");
                entry.title = obj.getString("title");
                entry.details = obj.getString("details");
                entry.done = obj.getBoolean("done");

                list.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}