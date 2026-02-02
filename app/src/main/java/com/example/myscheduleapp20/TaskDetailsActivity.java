package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TaskDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // חץ חזרה למעלה
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("פרטי משימה");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtTime = findViewById(R.id.txtTime);
        EditText edtDetails = findViewById(R.id.edtDetails);
        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);
        Button btnLogout = findViewById(R.id.btnLogout);

        // נתונים מהמסך הקודם
        String title = getIntent().getStringExtra("title");
        String time = getIntent().getStringExtra("time");
        String details = getIntent().getStringExtra("details");

        txtTitle.setText(title != null ? title : "");
        txtTime.setText("שעה: " + (time != null ? time : ""));
        edtDetails.setText(details != null ? details : "");

        // שמירת שינויים (כרגע רק חזרה אחורה עם נתון מעודכן)
        btnSaveChanges.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("updatedDetails", edtDetails.getText().toString());
            setResult(RESULT_OK, result);
            finish();
        });

        // התנתקות
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // חזרה עם החץ למעלה
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
