package com.example.myscheduleapp20;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ScheduleItem> items = new ArrayList<>();
    private ScheduleAdapter adapter;

    private ActivityResultLauncher<Intent> addItemLauncher;

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    private void scheduleReminder(long triggerAtMillis, String title, String details, int requestCode) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);
        intent.putExtra("notifId", requestCode);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        ensureNotificationPermission();

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddItem = findViewById(R.id.btnAddItem);
        RecyclerView recyclerSchedule = findViewById(R.id.recyclerSchedule);

        adapter = new ScheduleAdapter(items);
        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ScheduleActivity.this, MainActivity.class));
            finish();
        });

        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        String title = result.getData().getStringExtra("title");
                        String details = result.getData().getStringExtra("details");
                        String displayTime = result.getData().getStringExtra("displayTime");
                        long triggerAtMillis = result.getData().getLongExtra("triggerAtMillis", -1);

                        if (title == null) title = "";
                        if (details == null) details = "";
                        if (displayTime == null) displayTime = "";

                        if (!title.trim().isEmpty()) {
                            items.add(new ScheduleItem(title, displayTime, details));
                            adapter.notifyItemInserted(items.size() - 1);

                            if (triggerAtMillis > System.currentTimeMillis()) {
                                int requestCode = (int) System.currentTimeMillis();
                                try {
                                    scheduleReminder(triggerAtMillis, title, details, requestCode);
                                } catch (Exception e) {
                                    // לא מפילים את האפליקציה בגלל תזכורת
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
        );

        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, AddScheduleItemActivity.class);
            addItemLauncher.launch(intent);
        });
    }
}
