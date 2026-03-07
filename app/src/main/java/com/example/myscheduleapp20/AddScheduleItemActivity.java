package com.example.myscheduleapp20;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class AddScheduleItemActivity extends AppCompatActivity {

    private EditText etSubject, etStartTime, etEndTime, etNote;
    private Spinner spnDay;
    private RadioButton rbSchool, rbAfterSchool;
    private Button btnSave;

    private FirebaseFirestore db;

    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        db = FirebaseFirestore.getInstance();

        etSubject = findViewById(R.id.etSubject);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etNote = findViewById(R.id.etNote);

        spnDay = findViewById(R.id.spnDay);

        rbSchool = findViewById(R.id.rbSchool);
        rbAfterSchool = findViewById(R.id.rbAfterSchool);

        btnSave = findViewById(R.id.btnSave);

        setupDaySpinner();

        etStartTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void setupDaySpinner() {

        String[] days = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                days
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnDay.setAdapter(adapter);
    }

    private void showTimePicker() {

        Calendar c = Calendar.getInstance();

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    etStartTime.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    hourOfDay,
                                    minute
                            )
                    );
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveItem() {

        String subject = etSubject.getText().toString().trim();
        String start = etStartTime.getText().toString().trim();
        String end = etEndTime.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        String day = spnDay.getSelectedItem().toString();

        String type = rbAfterSchool.isChecked() ? "AFTER_SCHOOL" : "SCHOOL";

        if (subject.isEmpty() || start.isEmpty()) {
            Toast.makeText(this, "חובה למלא מקצוע ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) return;

        Calendar cal = Calendar.getInstance();

        if (selectedHour != -1) {

            cal.set(Calendar.HOUR_OF_DAY, selectedHour);
            cal.set(Calendar.MINUTE, selectedMinute);
            cal.set(Calendar.SECOND, 0);

        }

        long alarmTime = cal.getTimeInMillis();

        int alarmId = (int) (System.currentTimeMillis() & 0xfffffff);

        ScheduleItem item = new ScheduleItem(
                uid,
                day,
                type,
                1,
                subject,
                start,
                end,
                note,
                selectedHour,
                selectedMinute,
                alarmTime,
                alarmId
        );

        db.collection("users")
                .document(uid)
                .collection("scheduleItems")
                .add(item)
                .addOnSuccessListener(doc -> {

                    item.setId(doc.getId());

                    scheduleNotification(item);

                    finish();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show()
                );
    }

    private void scheduleNotification(ScheduleItem item) {

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (am == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", item.getSubjectName());

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                item.getAlarmId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (item.getAlarmTime() <= System.currentTimeMillis()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!am.canScheduleExactAlarms()) {
                Toast.makeText(this, "אין הרשאה להתראה מדויקת", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                item.getAlarmTime(),
                pi
        );
    }
}