package com.example.myscheduleapp20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Button btnLogout = findViewById(R.id.btnLogout);

        if (btnLogout == null) {
            Toast.makeText(this, "btnLogout לא נמצא ב-activity_schedule.xml", Toast.LENGTH_LONG).show();
            return;
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ScheduleActivity.this, MainActivity.class));
            finish();
        });
    }
}
