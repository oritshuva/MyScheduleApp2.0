package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ScheduleActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ViewPager + Tabs
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddTask = findViewById(R.id.fabAddTask);

        SchedulePagerAdapter pagerAdapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("רגיל");
            } else {
                tab.setText("אחרי בית ספר");
            }
        }).attach();

        // FAB (+) - פותח מסך הוספה לפי הטאב הנוכחי
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, AddScheduleItemActivity.class);

            String currentType = (viewPager.getCurrentItem() == 0) ? "רגיל" : "אחרי בית ספר";
            intent.putExtra("scheduleType", currentType);

            startActivity(intent);
        });

        // נתוני דמו (רק אם אין בכלל משימות)
        checkAndCreateDefaultSchedule();
    }

    // ===== תפריט עליון (כולל התנתקות) =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndCreateDefaultSchedule() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        createDefaultItem("מתמטיקה", "08:15", "חדר 204", "רגיל");
                        createDefaultItem("אנגלית", "10:00", "מבחן", "רגיל");
                        createDefaultItem("אימון כדורגל", "17:00", "מגרש", "אחרי בית ספר");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בבדיקת נתונים ראשוניים", Toast.LENGTH_SHORT).show()
                );
    }

    private void createDefaultItem(String title, String timeStr, String details, String type) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        long now = System.currentTimeMillis();
        int alarmId = (int) (now % Integer.MAX_VALUE);

        // שים לב: אם ScheduleItem אצלך נמצא ב-package אחר (model), עדכן את השורה הזאת
        com.example.myscheduleapp20.model.ScheduleItem item =
                new com.example.myscheduleapp20.model.ScheduleItem(title, timeStr, details, now, alarmId, type);

        db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .add(item);
    }
}