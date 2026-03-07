package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();

            return;
        }

        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
        finish();
    }
}