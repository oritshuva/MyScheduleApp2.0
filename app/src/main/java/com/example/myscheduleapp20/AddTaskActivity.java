package com.example.myscheduleapp20;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myscheduleapp20.model.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private final Calendar selected = Calendar.getInstance();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText edtTitle;
    private TextView txtChosenDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        edtTitle = findViewById(R.id.edtTitle);
        txtChosenDate = findViewById(R.id.txtChosenDate);

        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnSave = findViewById(R.id.btnSave);

        btnPickDate.setOnClickListener(v ->
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {
                            selected.set(Calendar.YEAR, year);
                            selected.set(Calendar.MONTH, month);
                            selected.set(Calendar.DAY_OF_MONTH, day);
                            updateDisplay();
                        },
                        selected.get(Calendar.YEAR),
                        selected.get(Calendar.MONTH),
                        selected.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        btnPickTime.setOnClickListener(v ->
                new TimePickerDialog(
                        this,
                        (view, hour, minute) -> {
                            selected.set(Calendar.HOUR_OF_DAY, hour);
                            selected.set(Calendar.MINUTE, minute);
                            updateDisplay();
                        },
                        selected.get(Calendar.HOUR_OF_DAY),
                        selected.get(Calendar.MINUTE),
                        true
                ).show()
        );

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void updateDisplay() {
        SimpleDateFormat fmt =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        txtChosenDate.setText(fmt.format(selected.getTime()));
    }

    private void saveTask() {

        String title = edtTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "יש להזין כותרת", Toast.LENGTH_SHORT).show();
            return;
        }

        long alarmTime = selected.getTimeInMillis();
        int alarmId = (int) (alarmTime % Integer.MAX_VALUE);

        Task task = new Task(title, alarmTime, alarmId);

        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("tasks")
                .document(uid)
                .collection("freeTasks")
                .add(task)
                .addOnSuccessListener(ref -> {
                    scheduleNotification(task);
                    finish();
                });
    }

    private void scheduleNotification(Task task) {

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                task.getAlarmId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            task.getAlarmTime(),
                            pendingIntent
                    );
                } else {
                    // אם אין הרשאה ל-Exact alarm – fallback רגיל
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            task.getAlarmTime(),
                            pendingIntent
                    );
                }

            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.getAlarmTime(),
                        pendingIntent
                );
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}