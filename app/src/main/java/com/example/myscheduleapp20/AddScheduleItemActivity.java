package com.example.myscheduleapp20;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        EditText edtTitle = findViewById(R.id.edtTitle);
        EditText edtDetails = findViewById(R.id.edtDetails);
        TextView txtChosenDate = findViewById(R.id.txtChosenDate);
        RadioGroup radioGroupType = findViewById(R.id.radioGroupType);
        Button btnSave = findViewById(R.id.btnSave);

        Intent in = getIntent();

        // מצב עריכה
        if (in != null && in.hasExtra("docId")) {
            docId = in.getStringExtra("docId");
            edtTitle.setText(in.getStringExtra("title"));
            edtDetails.setText(in.getStringExtra("details"));

            String type = in.getStringExtra("scheduleType");
            if ("אחרי בית ספר".equals(type)) {
                radioGroupType.check(R.id.radioAfterSchool);
            } else {
                radioGroupType.check(R.id.radioNormal); // <-- תואם ל-XML שלך
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

            // חשוב: הערך שנשמר חייב להתאים למה שהטאבים ב-ScheduleActivity מחפשים
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

        if (docId != null && !docId.isEmpty()) {
            db.collection("tasks")
                    .document(uid)
                    .collection("userTasks")
                    .document(docId)
                    .set(item)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            db.collection("tasks")
                    .document(uid)
                    .collection("userTasks")
                    .add(item)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "נשמר בלו\"ז", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

    private void updateDateTimeDisplay(TextView tv) {
        String dt = String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d | %02d:%02d",
                selected.get(Calendar.DAY_OF_MONTH),
                selected.get(Calendar.MONTH) + 1,
                selected.get(Calendar.YEAR),
                selected.get(Calendar.HOUR_OF_DAY),
                selected.get(Calendar.MINUTE)
        );
        tv.setText(dt);
    }
}
