package com.example.myscheduleapp20;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private LinearLayout layoutDays;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Map<String, Spinner> daySpinners = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        layoutDays = findViewById(R.id.layoutDays);
        Button btnSave = findViewById(R.id.btnSaveTemplate);

        createDaySpinners();

        btnSave.setOnClickListener(v -> saveTemplate());
    }

    private void createDaySpinners() {

        String[] days = {
                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday"
        };

        Integer[] periods = {
                0,1,2,3,4,5,6,7,8,9,10
        };

        for (String day : days) {

            TextView tv = new TextView(this);
            tv.setText(day);
            tv.setTextSize(18);

            Spinner spinner = new Spinner(this);

            ArrayAdapter<Integer> adapter =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            periods
                    );

            spinner.setAdapter(adapter);

            layoutDays.addView(tv);
            layoutDays.addView(spinner);

            daySpinners.put(day, spinner);
        }
    }

    private void saveTemplate() {

        String uid = auth.getUid();

        if (uid == null) return;

        Map<String, Object> template = new HashMap<>();

        for (String day : daySpinners.keySet()) {

            Spinner spinner = daySpinners.get(day);

            int count = (int) spinner.getSelectedItem();

            template.put(day, count);
        }

        db.collection("users")
                .document(uid)
                .collection("scheduleTemplate")
                .document("week")
                .set(template)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "נשמר בהצלחה",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "שגיאה בשמירה",
                                Toast.LENGTH_SHORT
                        ).show());
    }
}