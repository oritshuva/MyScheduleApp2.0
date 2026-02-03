package com.example.myscheduleapp20;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddScheduleItemActivity extends AppCompatActivity {

    private final Calendar selected = Calendar.getInstance();
    private boolean datePicked = false;
    private boolean timePicked = false;

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

            Intent result = new Intent();
            result.putExtra("title", title);
            result.putExtra("details", details);
            result.putExtra("triggerAtMillis", triggerAtMillis);

            // לתצוגה בלוז (מחרוזת נוחה)
            String displayTime = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d %02d:%02d",
                    selected.get(Calendar.DAY_OF_MONTH),
                    (selected.get(Calendar.MONTH) + 1),
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE));
            result.putExtra("displayTime", displayTime);

            setResult(RESULT_OK, result);
            finish();
        });
    }
}
