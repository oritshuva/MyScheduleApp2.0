package com.example.myscheduleapp20;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditScheduleItemActivity extends AppCompatActivity {

    private EditText etSubject;
    private EditText etStart;
    private EditText etEnd;
    private EditText etNote;

    private Button btnSave;

    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        etSubject = findViewById(R.id.etSubject);
        etStart = findViewById(R.id.etStartTime);
        etEnd = findViewById(R.id.etEndTime);
        etNote = findViewById(R.id.etNote);

        btnSave = findViewById(R.id.btnSave);

        itemId = getIntent().getStringExtra("itemId");

        etSubject.setText(getIntent().getStringExtra("subject"));
        etStart.setText(getIntent().getStringExtra("start"));
        etEnd.setText(getIntent().getStringExtra("end"));
        etNote.setText(getIntent().getStringExtra("note"));

        btnSave.setOnClickListener(v -> updateItem());
    }

    private void updateItem() {

        String subject = etSubject.getText().toString().trim();
        String start = etStart.getText().toString().trim();
        String end = etEnd.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();

        data.put("subjectName", subject);
        data.put("startTime", start);
        data.put("endTime", end);
        data.put("note", note);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("scheduleItems")
                .document(itemId)
                .update(data)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "עודכן", Toast.LENGTH_SHORT).show();
                    finish();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show());
    }
}