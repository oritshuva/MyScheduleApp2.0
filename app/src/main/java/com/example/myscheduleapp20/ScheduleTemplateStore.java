package com.example.myscheduleapp20;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTemplateStore {

    private static final String PREF_NAME = "template_store";
    private static final String KEY_PERIODS = "period_times";

    private final SharedPreferences prefs;

    public ScheduleTemplateStore(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void savePeriods(List<String> periods) {
        JSONArray array = new JSONArray();
        for (String p : periods) {
            array.put(p);
        }
        prefs.edit().putString(KEY_PERIODS, array.toString()).apply();
    }

    public List<String> loadPeriods() {
        List<String> result = new ArrayList<>();
        String json = prefs.getString(KEY_PERIODS, null);
        if (json == null) return result;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                result.add(array.getString(i));
            }
        } catch (Exception ignored) {}

        return result;
    }

    public boolean isTemplateExists() {
        String json = prefs.getString(KEY_PERIODS, null);
        if (json == null) return false;

        try {
            JSONArray array = new JSONArray(json);
            return array.length() > 0; // חייב לפחות שעה אחת
        } catch (Exception e) {
            return false;
        }
    }
}