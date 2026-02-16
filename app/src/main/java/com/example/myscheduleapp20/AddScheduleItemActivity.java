package com.example.myscheduleapp20;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddScheduleItemActivity extends AppCompatActivity {

    private final Calendar selected = Calendar.getInstance();
    private boolean datePicked = false;
    private boolean timePicked = false;

    private String docId = null;

    private static String formatDisplayTime(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(millis);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        EditText edtTitle = findViewById(R.id.edtTitle);
        EditText edtDetails = findViewById(R.id.edtDetails);

        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        TextView txtChosenDate = findViewById(R.id.txtChosenDate);
        TextView txtChosenTime = findViewById(R.id.txtChosenTime);

        Button btnSave = findViewById(R.id.btnSave);

        // מצב עריכה: מילוי נתונים מה-intent
        Intent in = getIntent();
        if (in != null) {
            docId = in.getStringExtra("docId");
            String t = in.getStringExtra("title");
            String d = in.getStringExtra("details");
            long time = in.getLongExtra("triggerAtMillis", -1);

            if (t != null) edtTitle.setText(t);
            if (d != null) edtDetails.setText(d);

            if (docId != null && !docId.isEmpty()) {
                btnSave.setText("עדכן");
            }

            if (time > 0) {
                selected.setTimeInMillis(time);
                datePicked = true;
                timePicked = true;

                txtChosenDate.setText(String.format(Locale.getDefault(),
                        "תאריך: %02d/%02d/%04d",
                        selected.get(Calendar.DAY_OF_MONTH),
                        (selected.get(Calendar.MONTH) + 1),
                        selected.get(Calendar.YEAR)));

                txtChosenTime.setText(String.format(Locale.getDefault(),
                        "שעה: %02d:%02d",
                        selected.get(Calendar.HOUR_OF_DAY),
                        selected.get(Calendar.MINUTE)));
            }
        }

        btnPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selected.set(Calendar.YEAR, year);
                        selected.set(Calendar.MONTH, month);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        datePicked = true;
                        txtChosenDate.setText(String.format(Locale.getDefault(),
                                "תאריך: %02d/%02d/%04d", dayOfMonth, (month + 1), year));
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnPickTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selected.set(Calendar.MINUTE, minute);
                        selected.set(Calendar.SECOND, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        timePicked = true;
                        txtChosenTime.setText(String.format(Locale.getDefault(),
                                "שעה: %02d:%02d", hourOfDay, minute));
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            ).show();
        });

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "יש למלא כותרת", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!datePicked || !timePicked) {
                Toast.makeText(this, "בחר תאריך ושעה", Toast.LENGTH_SHORT).show();
                return;
            }

            long triggerAtMillis = selected.getTimeInMillis();
            if (triggerAtMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "בחר זמן עתידי", Toast.LENGTH_SHORT).show();
                return;
            }

            // קריאה לפונקציית זימון ההתראה
            scheduleNotification(title, details, triggerAtMillis);

            Intent result = new Intent();
            if (docId != null && !docId.isEmpty()) result.putExtra("docId", docId);
            result.putExtra("title", title);
            result.putExtra("details", details);
            result.putExtra("triggerAtMillis", triggerAtMillis);
            result.putExtra("displayTime", formatDisplayTime(triggerAtMillis));

            setResult(RESULT_OK, result);
            Toast.makeText(this, "המשימה נשמרה והתראה הוגדרה!", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void scheduleNotification(String title, String details, long timeInMillis) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);

        // מזהה ייחודי להתראה (כדי שנוכל לקבוע כמה התראות במקביל)
        int notifId = (int) (timeInMillis / 1000);
        intent.putExtra("notifId", notifId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // בדיקת הרשאה לאנדרואיד 12 ומעלה עבור התראות מדויקות
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(i);
                    return;
                }
            }

            // קביעת ההתראה לזמן המדויק
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }
}