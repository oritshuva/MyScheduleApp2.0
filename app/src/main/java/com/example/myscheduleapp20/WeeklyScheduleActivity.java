package com.example.myscheduleapp20;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyScheduleActivity extends AppCompatActivity {

    private final SimpleDateFormat viewFmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private Calendar selectedDay;

    private TextView tvDayTitle;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnSchool, btnAfter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_schedule);

        selectedDay = Calendar.getInstance();

        tvDayTitle = findViewById(R.id.tvDayTitle);
        toggleGroup = findViewById(R.id.toggleGroup);
        btnSchool = findViewById(R.id.btnSchool);
        btnAfter = findViewById(R.id.btnAfterSchool);

        ImageButton btnPrev = findViewById(R.id.btnPrevDay);
        ImageButton btnNext = findViewById(R.id.btnNextDay);

        FloatingActionButton fab = findViewById(R.id.fabAddWeekly);

        // ברירת מחדל: לימודים
        toggleGroup.check(btnSchool.getId());

        btnPrev.setOnClickListener(v -> {
            selectedDay.add(Calendar.DAY_OF_MONTH, -1);
            updateTitle();
        });

        btnNext.setOnClickListener(v -> {
            selectedDay.add(Calendar.DAY_OF_MONTH, +1);
            updateTitle();
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String type = (checkedId == btnSchool.getId()) ? "לימודים" : "אחרי בית ספר";
            Toast.makeText(this, "נבחר: " + type, Toast.LENGTH_SHORT).show();
        });

        fab.setOnClickListener(v ->
                Toast.makeText(this, "בשלב הבא: הוספת פריט", Toast.LENGTH_SHORT).show()
        );

        updateTitle();
    }

    private void updateTitle() {
        tvDayTitle.setText(dayHebName(selectedDay) + " " + viewFmt.format(selectedDay.getTime()));
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
