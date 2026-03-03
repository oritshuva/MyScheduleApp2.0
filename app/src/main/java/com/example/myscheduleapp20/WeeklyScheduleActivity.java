package com.example.myscheduleapp20;

import android.content.Intent;
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
    private ScheduleTemplateStore templateStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_schedule);

        templateStore = new ScheduleTemplateStore(this);

        if (!templateStore.isTemplateExists()) {
            startActivity(new Intent(this, TemplateSetupActivity.class));
            finish();
            return;
        }

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
            selectedDay.set(Calendar.DAY_OF_WEEK, getDayFromId(checkedId));
            loadFromStorage();
        });

        fab.setOnClickListener(v -> {
            if (!isSchoolSelected) {
                addAfterSchoolItem();
            }
        });

        loadFromStorage();
    }

    private int getDayFromId(int id) {
        if (id == R.id.daySun) return Calendar.SUNDAY;
        if (id == R.id.dayMon) return Calendar.MONDAY;
        if (id == R.id.dayTue) return Calendar.TUESDAY;
        if (id == R.id.dayWed) return Calendar.WEDNESDAY;
        if (id == R.id.dayThu) return Calendar.THURSDAY;
        if (id == R.id.dayFri) return Calendar.FRIDAY;
        return Calendar.SATURDAY;
    }

    private void addAfterSchoolItem() {

        ScheduleEntry entry = new ScheduleEntry(
                "id_" + System.currentTimeMillis(),
                getDayKey(),
                ScheduleEntry.TYPE_TASK,
                "משימה חדשה",
                "",
                false,
                -1
        );

        List<ScheduleEntry> all = store.loadAll();
        all.add(entry);
        store.saveAll(all);

        loadFromStorage();
    }

    private void loadFromStorage() {

        currentItems.clear();

        List<String> periodTimes = templateStore.loadPeriods();
        List<ScheduleEntry> all = store.loadAll();

        if (isSchoolSelected) {

            for (int i = 0; i < periodTimes.size(); i++) {

                ScheduleEntry found = null;

                for (ScheduleEntry entry : all) {
                    if (entry.dayKey.equals(getDayKey())
                            && entry.type.equals(ScheduleEntry.TYPE_LESSON)
                            && entry.periodIndex == i) {

                        found = entry;
                        break;
                    }
                }

                if (found != null) {
                    currentItems.add(found);
                } else {
                    currentItems.add(
                            new ScheduleEntry(
                                    "slot_" + getDayKey() + "_" + i,
                                    getDayKey(),
                                    ScheduleEntry.TYPE_LESSON,
                                    "",
                                    periodTimes.get(i),
                                    false,
                                    i
                            )
                    );
                }
            }

        } else {

            for (ScheduleEntry entry : all) {
                if (entry.dayKey.equals(getDayKey())
                        && entry.type.equals(ScheduleEntry.TYPE_TASK)) {

                    currentItems.add(entry);
                }
            }
        }

        adapter.notifyDataSetChanged();
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