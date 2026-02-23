package com.example.myscheduleapp20;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class AddScheduleItemActivity extends AppCompatActivity {

    private final Calendar selected = Calendar.getInstance();
    private boolean datePicked = false;
    private boolean timePicked = false;
    private String docId = null;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();

        EditText edtTitle = findViewById(R.id.edtTitle);
        EditText edtDetails = findViewById(R.id.edtDetails);
        TextView txtChosenDate = findViewById(R.id.txtChosenDate);
        RadioGroup radioGroupType = findViewById(R.id.radioGroupType);
        Button btnSave = findViewById(R.id.btnSave);

        Intent in = getIntent();
        if (in != null && in.hasExtra("docId")) {
            docId = in.getStringExtra("docId");
            edtTitle.setText(in.getStringExtra("title"));
            edtDetails.setText(in.getStringExtra("details"));

            String type = in.getStringExtra("scheduleType");
            if ("אחרי בית ספר".equals(type)) {
                radioGroupType.check(R.id.radioAfterSchool);
            } else {
                radioGroupType.check(R.id.radioNormal);
            }

            long triggerAtMillis = in.getLongExtra("triggerAtMillis", -1L);
            if (triggerAtMillis > 0) {
                selected.setTimeInMillis(triggerAtMillis);
                datePicked = true;
                timePicked = true;
                updateDateTimeDisplay(txtChosenDate);
            }
        }

        findViewById(R.id.btnPickDate).setOnClickListener(v -> {
            new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        selected.set(Calendar.YEAR, y);
                        selected.set(Calendar.MONTH, m);
                        selected.set(Calendar.DAY_OF_MONTH, d);
                        datePicked = true;
                        updateDateTimeDisplay(txtChosenDate);
                    },
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH),
                    selected.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        findViewById(R.id.btnPickTime).setOnClickListener(v -> {
            new TimePickerDialog(
                    this,
                    (view, h, min) -> {
                        selected.set(Calendar.HOUR_OF_DAY, h);
                        selected.set(Calendar.MINUTE, min);
                        selected.set(Calendar.SECOND, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        timePicked = true;
                        updateDateTimeDisplay(txtChosenDate);
                    },
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE),
                    true
            ).show();
        });

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();
            String type = (radioGroupType.getCheckedRadioButtonId() == R.id.radioAfterSchool)
                    ? "אחרי בית ספר"
                    : "רגיל";

            if (title.isEmpty() || !datePicked || !timePicked) {
                Toast.makeText(this, "נא למלא שם, תאריך ושעה", Toast.LENGTH_SHORT).show();
                return;
            }

            String displayTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE)
            );

            long selectedMillis = selected.getTimeInMillis();
            int alarmId = (int) (selectedMillis % Integer.MAX_VALUE);

            ScheduleItem item = new ScheduleItem(
                    title,
                    displayTime,
                    details,
                    selectedMillis,
                    alarmId,
                    type
            );

            saveToFirebase(item);
        });
    }

    private void saveToFirebase(ScheduleItem item) {
        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        // אם ה-Firestore Rules שלך מוגדרים תחת /users/{uid}/tasks
        // תחליף את הנתיב הזה ל: db.collection("users").document(uid).collection("tasks")
        if (docId != null && !docId.isEmpty()) {
            db.collection("tasks").document(uid).collection("userTasks").document(docId)
                    .set(item)
                    .addOnSuccessListener(aVoid -> {
                        saveTaskToLocalProvider(docId, item); // <-- חדש (שמירה מקומית)
                        scheduleNotification(item);
                        Toast.makeText(this, "עודכן ונקבעה תזכורת", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            db.collection("tasks").document(uid).collection("userTasks")
                    .add(item)
                    .addOnSuccessListener(ref -> {
                        item.setId(ref.getId());
                        saveTaskToLocalProvider(ref.getId(), item); // <-- חדש (שמירה מקומית)
                        scheduleNotification(item);
                        Toast.makeText(this, "נשמר ונקבעה תזכורת", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    // ===== שלב 5ב: פונקציה חדשה לשמירה מקומית דרך ContentProvider =====
    private void saveTaskToLocalProvider(String firebaseDocId, ScheduleItem item) {
        try {
            ContentValues values = new ContentValues();
            values.put(ScheduleContract.TaskEntry.COLUMN_FIREBASE_DOC_ID, firebaseDocId);
            values.put(ScheduleContract.TaskEntry.COLUMN_TITLE, item.getTitle());
            values.put(ScheduleContract.TaskEntry.COLUMN_DISPLAY_TIME, item.getDisplayTime());
            values.put(ScheduleContract.TaskEntry.COLUMN_DETAILS, item.getDetails());
            values.put(ScheduleContract.TaskEntry.COLUMN_TIME_MILLIS, item.getTime());
            values.put(ScheduleContract.TaskEntry.COLUMN_ALARM_ID, item.getAlarmId());
            values.put(ScheduleContract.TaskEntry.COLUMN_SCHEDULE_TYPE, item.getScheduleType());

            int updated = getContentResolver().update(
                    ScheduleContract.TaskEntry.CONTENT_URI,
                    values,
                    ScheduleContract.TaskEntry.COLUMN_FIREBASE_DOC_ID + "=?",
                    new String[]{firebaseDocId}
            );

            if (updated == 0) {
                getContentResolver().insert(ScheduleContract.TaskEntry.CONTENT_URI, values);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "שגיאה בשמירה מקומית", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(ScheduleItem item) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", item.getTitle());
        intent.putExtra("details", item.getDetails());
        intent.putExtra("notifId", item.getAlarmId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                item.getAlarmId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = item.getTime();
        if (triggerAt <= System.currentTimeMillis()) {
            Toast.makeText(this, "נבחר זמן שכבר עבר", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                    Toast.makeText(this, "התראה נקבעה. אשר Alarms & reminders לדיוק מלא", Toast.LENGTH_LONG).show();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                Toast.makeText(this, "התראה נקבעה (fallback)", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(this, "שגיאה בקביעת התראה", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "שגיאה בקביעת התראה", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                try {
                    Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateDateTimeDisplay(TextView tv) {
        tv.setText(String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d | %02d:%02d",
                selected.get(Calendar.DAY_OF_MONTH),
                selected.get(Calendar.MONTH) + 1,
                selected.get(Calendar.YEAR),
                selected.get(Calendar.HOUR_OF_DAY),
                selected.get(Calendar.MINUTE)
        ));
    }
}
