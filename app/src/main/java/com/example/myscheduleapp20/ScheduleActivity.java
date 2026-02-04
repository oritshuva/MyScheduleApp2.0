package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ScheduleItem> items = new ArrayList<>();
    private ScheduleAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    private ListenerRegistration tasksListener;
    private ActivityResultLauncher<Intent> addItemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // UI
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddItem = findViewById(R.id.btnAddItem);
        RecyclerView recyclerSchedule = findViewById(R.id.recyclerSchedule);

        adapter = new ScheduleAdapter(items);
        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            goToMain();
            return;
        }
        uid = auth.getCurrentUser().getUid();

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToMain();
        });

        // ✅ מאזין בזמן אמת: מעדכן רשימה לפי Firestore
        startTasksListener();

        // קבלת תוצאה ממסך הוספת משימה
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
                        if (triggerAtMillis <= 0) triggerAtMillis = System.currentTimeMillis();

                        if (!title.trim().isEmpty()) {
                            saveTaskToFirestore(title, details, displayTime, triggerAtMillis);
                        }
                    }
                }
        );

        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleItemActivity.class);
            addItemLauncher.launch(intent);
        });
    }

    private void startTasksListener() {
        if (tasksListener != null) return;

        tasksListener = db.collection("users")
                .document(uid)
                .collection("tasks")
                .orderBy("time") // מסדר כרונולוגית
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (snapshot == null) return;

                    items.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String title = doc.getString("title");
                        String details = doc.getString("details");
                        String displayTime = doc.getString("displayTime");
                        Long timeMillis = doc.getLong("time");

                        if (title == null) title = "";
                        if (details == null) details = "";
                        if (displayTime == null) displayTime = "";

                        // אם אין displayTime (משימות ישנות) - ניצור מתאריך time
                        if (displayTime.trim().isEmpty() && timeMillis != null) {
                            displayTime = android.text.format.DateFormat
                                    .format("dd/MM/yyyy HH:mm", new Date(timeMillis))
                                    .toString();
                        }

                        items.add(new ScheduleItem(title, displayTime, details));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void saveTaskToFirestore(String title, String details, String displayTime, long timeMillis) {
        HashMap<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("details", details);
        task.put("displayTime", displayTime); // ✅ תאריך+שעה כתצוגה
        task.put("time", timeMillis);         // ✅ זמן מספרי לסידור/התראות

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .add(task)
                .addOnFailureListener(err -> {
                    err.printStackTrace();
                    Toast.makeText(this, "שגיאה בשמירה ל-Firestore", Toast.LENGTH_SHORT).show();
                });

        // ✅ לא מוסיפים ידנית לרשימה — המאזין יעדכן לבד
    }

    private void goToMain() {
        Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
    }
}
