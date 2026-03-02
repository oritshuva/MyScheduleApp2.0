package com.example.myscheduleapp20;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyScheduleActivity extends AppCompatActivity {

    private final SimpleDateFormat viewFmt =
            new SimpleDateFormat("dd/MM", Locale.getDefault());

    private Calendar selectedDay;

    private TextView tvDayTitle;
    private TextView tvEmpty;

    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButtonToggleGroup dayToggleGroup;

    private MaterialButton btnSchool;

    private RecyclerView rvWeekly;

    private final ArrayList<ScheduleEntry> currentItems = new ArrayList<>();
    private SimpleStringAdapter adapter;

    private boolean isSchoolSelected = true;

    private WeeklyScheduleStore store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_schedule);

        selectedDay = Calendar.getInstance();
        store = new WeeklyScheduleStore(this);

        tvDayTitle = findViewById(R.id.tvDayTitle);
        tvEmpty = findViewById(R.id.tvEmpty);

        toggleGroup = findViewById(R.id.toggleGroup);
        dayToggleGroup = findViewById(R.id.dayToggleGroup);

        btnSchool = findViewById(R.id.btnSchool);

        rvWeekly = findViewById(R.id.rvWeekly);
        FloatingActionButton fab = findViewById(R.id.fabAddWeekly);

        adapter = new SimpleStringAdapter(
                currentItems,
                entry -> saveAllData()
        );

        rvWeekly.setLayoutManager(new LinearLayoutManager(this));
        rvWeekly.setAdapter(adapter);

        // ברירת מחדל
        toggleGroup.check(btnSchool.getId());
        dayToggleGroup.check(R.id.daySun);
        selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            isSchoolSelected = (checkedId == R.id.btnSchool);
            loadFromStorage();
        });

        dayToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.daySun) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            } else if (checkedId == R.id.dayMon) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            } else if (checkedId == R.id.dayTue) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);

            } else if (checkedId == R.id.dayWed) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

            } else if (checkedId == R.id.dayThu) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

            } else if (checkedId == R.id.dayFri) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

            } else if (checkedId == R.id.daySat) {
                selectedDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            }

            loadFromStorage();
        });

        fab.setOnClickListener(v -> addNewItem());

        loadFromStorage();
    }

    private void addNewItem() {

        ScheduleEntry entry = new ScheduleEntry(
                "id" + System.currentTimeMillis(),
                getDayKey(),
                isSchoolSelected ?
                        ScheduleEntry.TYPE_LESSON :
                        ScheduleEntry.TYPE_TASK,
                isSchoolSelected ?
                        "שיעור חדש" :
                        "משימה חדשה",
                "",
                false
        );

        List<ScheduleEntry> all = store.loadAll();
        all.add(entry);
        store.saveAll(all);

        loadFromStorage();
    }

    private void loadFromStorage() {

        currentItems.clear();

        List<ScheduleEntry> all = store.loadAll();

        for (ScheduleEntry entry : all) {
            if (entry.dayKey.equals(getDayKey())
                    && entry.type.equals(
                    isSchoolSelected ?
                            ScheduleEntry.TYPE_LESSON :
                            ScheduleEntry.TYPE_TASK)) {

                currentItems.add(entry);
            }
        }

        adapter.notifyDataChanged();
        updateEmptyState();
        updateTitle();
    }

    private void saveAllData() {
        store.saveAll(store.loadAll());
    }

    private void updateEmptyState() {
        tvEmpty.setVisibility(
                currentItems.isEmpty()
                        ? TextView.VISIBLE
                        : TextView.GONE
        );
    }

    private void updateTitle() {
        tvDayTitle.setText(
                dayHebName(selectedDay) + " " +
                        viewFmt.format(selectedDay.getTime()));
    }

    private String getDayKey() {
        switch (selectedDay.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY: return "SUNDAY";
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            case Calendar.SATURDAY: return "SATURDAY";
        }
        return "UNKNOWN";
    }

    private String dayHebName(Calendar c) {
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY: return "א׳";
            case Calendar.MONDAY: return "ב׳";
            case Calendar.TUESDAY: return "ג׳";
            case Calendar.WEDNESDAY: return "ד׳";
            case Calendar.THURSDAY: return "ה׳";
            case Calendar.FRIDAY: return "ו׳";
            case Calendar.SATURDAY: return "ש׳";
        }
        return "";
    }
}