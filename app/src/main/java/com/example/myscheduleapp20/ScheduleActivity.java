package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ScheduleItem> items = new ArrayList<>();
    private ScheduleAdapter adapter;

    private ActivityResultLauncher<Intent> addItemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddItem = findViewById(R.id.btnAddItemx);
        RecyclerView recyclerSchedule = findViewById(R.id.recyclerSchedule);

        // הגדרת הרשימה
        adapter = new ScheduleAdapter(items);
        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);

        // התנתקות
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ScheduleActivity.this, MainActivity.class));
            finish();
        });

        // מקבל חזרה את המשימה מהמסך השני
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String title = result.getData().getStringExtra("title");
                        String time = result.getData().getStringExtra("time");

                        if (title != null && !title.trim().isEmpty() && time != null && !time.trim().isEmpty()) {
                            items.add(new ScheduleItem(title, time));
                            adapter.notifyItemInserted(items.size() - 1);
                        }
                    }
                }
        );

        // מעבר למסך “הוספת משימה”
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, AddScheduleItemActivity.class);
            addItemLauncher.launch(intent);
        });
    }
}
