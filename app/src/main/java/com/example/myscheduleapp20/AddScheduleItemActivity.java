package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddScheduleItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        EditText edtTitle = findViewById(R.id.edtTitle);
        EditText edtTime = findViewById(R.id.edtTime);
        EditText edtDetails = findViewById(R.id.edtDetails);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String time = edtTime.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();

            if (title.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "יש למלא כותרת ושעה", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent result = new Intent();
            result.putExtra("title", title);
            result.putExtra("time", time);
            result.putExtra("details", details);

            setResult(RESULT_OK, result);
            finish(); // חוזר למסך הלוז
        });
    }
}
