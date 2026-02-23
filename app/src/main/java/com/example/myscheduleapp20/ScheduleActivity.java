package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private final List<ScheduleItem> allItems = new ArrayList<>();
    private final List<ScheduleItem> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration tasksListener;

    private String currentFilter = "רגיל";

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

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScheduleAdapter(filteredList, new ScheduleAdapter.OnItemActionListener() {
            @Override
            public void onClick(ScheduleItem item) {
                Intent intent = new Intent(ScheduleActivity.this, AddScheduleItemActivity.class);
                intent.putExtra("docId", item.getId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("details", item.getDetails());
                intent.putExtra("scheduleType", item.getScheduleType());
                intent.putExtra("displayTime", item.getDisplayTime());
                intent.putExtra("triggerAtMillis", item.getTimeMillis()); // אם המסך השני משתמש בזה
                startActivity(intent);
            }

            @Override
            public void onLongClick(ScheduleItem item) {
                String uid = mAuth.getUid();
                if (uid == null) return;

                db.collection("tasks")
                        .document(uid)
                        .collection("userTasks")
                        .document(item.getId())
                        .delete()
                        .addOnFailureListener(e ->
                                Toast.makeText(ScheduleActivity.this, "מחיקה נכשלה", Toast.LENGTH_SHORT).show()
                        );
            }
        });

        recyclerView.setAdapter(adapter);

        // Tabs
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = (tab.getPosition() == 0) ? "רגיל" : "אחרי בית ספר";
                filterTasks();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // FAB (+)
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, AddScheduleItemActivity.class);
            intent.putExtra("scheduleType", currentFilter);
            startActivity(intent);
        });

        // יצירת נתוני דמו רק אם אין בכלל משימות
        checkAndCreateDefaultSchedule();
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachTasksListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
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

        // אם בעתיד תרצה אזעקות אמיתיות - כדאי לשמור כאן זמן אמיתי במילישניות
        long now = System.currentTimeMillis();
        int alarmId = (int) (now % Integer.MAX_VALUE);

        ScheduleItem item = new ScheduleItem(title, timeStr, details, now, alarmId, type);

        db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .add(item);
    }

    private void attachTasksListener() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }

        tasksListener = db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null) return;

                    allItems.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            ScheduleItem item = doc.toObject(ScheduleItem.class);
                            if (item == null) continue;

                            item.setId(doc.getId());

                            if (item.getScheduleType() == null || item.getScheduleType().trim().isEmpty()) {
                                item.setScheduleType("רגיל");
                            }

                            allItems.add(item);
                        } catch (Exception ignored) {
                            // מדלגים על מסמך לא תקין כדי לא להפיל את כל הרשימה
                        }
                    }

                    filterTasks();
                });
    }

    private void filterTasks() {
        filteredList.clear();

        for (ScheduleItem item : allItems) {
            String type = item.getScheduleType();
            if (type == null) type = "רגיל";

            if (type.equals(currentFilter)) {
                filteredList.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
