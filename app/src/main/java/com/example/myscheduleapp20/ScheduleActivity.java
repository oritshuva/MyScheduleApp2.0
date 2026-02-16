package com.example.myscheduleapp20;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ScheduleItem> items = new ArrayList<>();
    private ScheduleAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    private ListenerRegistration tasksListener;
    private ActivityResultLauncher<Intent> addEditLauncher;

    private static String safeStr(String s) { return s == null ? "" : s; }

    private static String formatDisplayTime(long millis) {
        if (millis <= 0) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(millis);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        ensureNotificationPermission();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            goToMainAndFinish();
            return;
        }
        uid = user.getUid();

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddItem = findViewById(R.id.btnAddItem);
        RecyclerView recyclerSchedule = findViewById(R.id.recyclerSchedule);

        adapter = new ScheduleAdapter(items, new ScheduleAdapter.OnItemActionListener() {
            @Override
            public void onClick(ScheduleItem item) {
                openEdit(item);
            }

            @Override
            public void onLongClick(ScheduleItem item) {
                confirmDelete(item);
            }
        });

        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            // חשוב: לעצור listener לפני שיוצאים
            detachTasksListener();

            auth.signOut();
            goToMainAndFinish();
        });

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                    Intent data = result.getData();

                    String docId = safeStr(data.getStringExtra("docId")); // ריק = חדש
                    String title = safeStr(data.getStringExtra("title"));
                    String details = safeStr(data.getStringExtra("details"));
                    long triggerAtMillis = data.getLongExtra("triggerAtMillis", -1);

                    if (title.trim().isEmpty() || triggerAtMillis <= 0) {
                        Toast.makeText(this, "נתונים לא תקינים", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String displayTime = formatDisplayTime(triggerAtMillis);

                    boolean isEdit = !docId.isEmpty();

                    if (!isEdit) {
                        // ---- ADD חדש ----
                        // יוצרים מראש docId כדי לגזור ממנו alarmId קבוע
                        String newDocId = db.collection("users").document(uid).collection("tasks").document().getId();
                        int alarmId = Math.abs(newDocId.hashCode());

                        Map<String, Object> task = new HashMap<>();
                        task.put("title", title);
                        task.put("details", details);
                        task.put("displayTime", displayTime);
                        task.put("time", triggerAtMillis);   // number
                        task.put("alarmId", alarmId);        // number

                        db.collection("users")
                                .document(uid)
                                .collection("tasks")
                                .document(newDocId)
                                .set(task)
                                .addOnSuccessListener(v -> {
                                    if (triggerAtMillis > System.currentTimeMillis()) {
                                        scheduleReminder(triggerAtMillis, title, details, alarmId);
                                    }
                                    Toast.makeText(this, "נשמר!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        // ---- EDIT ----
                        // קוראים alarmId קיים (אם אין, מייצרים קבוע מה docId)
                        db.collection("users")
                                .document(uid)
                                .collection("tasks")
                                .document(docId)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    Long alarmIdLong = snapshot.getLong("alarmId");
                                    int alarmId = (alarmIdLong != null) ? alarmIdLong.intValue() : Math.abs(docId.hashCode());

                                    // מבטלים התראה קודמת כדי שלא יהיו 2
                                    cancelReminder(alarmId);

                                    Map<String, Object> task = new HashMap<>();
                                    task.put("title", title);
                                    task.put("details", details);
                                    task.put("displayTime", displayTime);
                                    task.put("time", triggerAtMillis); // number
                                    task.put("alarmId", alarmId);      // נשאר אותו הדבר

                                    db.collection("users")
                                            .document(uid)
                                            .collection("tasks")
                                            .document(docId)
                                            .update(task)
                                            .addOnSuccessListener(v -> {
                                                if (triggerAtMillis > System.currentTimeMillis()) {
                                                    scheduleReminder(triggerAtMillis, title, details, alarmId);
                                                }
                                                Toast.makeText(this, "עודכן!", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "שגיאה בקריאת משימה לעדכון", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
        );

        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleItemActivity.class);
            addEditLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachTasksListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachTasksListener();
    }

    private void attachTasksListener() {
        detachTasksListener();

        tasksListener = db.collection("users")
                .document(uid)
                .collection("tasks")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                        Toast.makeText(this, "שגיאה בטעינת משימות (בדוק time=number בכל המסמכים)", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snap == null) return;

                    items.clear();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String id = doc.getId();
                        String title = safeStr(doc.getString("title"));
                        String details = safeStr(doc.getString("details"));

                        Long t = doc.getLong("time");
                        long timeMillis = (t != null) ? t : 0L;

                        String displayTime = safeStr(doc.getString("displayTime"));
                        if (displayTime.isEmpty()) displayTime = formatDisplayTime(timeMillis);

                        Long alarmIdLong = doc.getLong("alarmId");
                        int alarmId = (alarmIdLong != null) ? alarmIdLong.intValue() : Math.abs(id.hashCode());

                        items.add(new ScheduleItem(id, title, displayTime, details, timeMillis, alarmId));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void detachTasksListener() {
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
    }

    private void openEdit(ScheduleItem item) {
        Intent intent = new Intent(this, AddScheduleItemActivity.class);
        intent.putExtra("docId", item.getId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("details", item.getDetails());
        intent.putExtra("triggerAtMillis", item.getTimeMillis());
        addEditLauncher.launch(intent);
    }

    private void confirmDelete(ScheduleItem item) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת משימה")
                .setMessage("למחוק את \"" + item.getTitle() + "\"?")
                .setNegativeButton("ביטול", null)
                .setPositiveButton("מחק", (d, which) -> deleteTask(item))
                .show();
    }

    private void deleteTask(ScheduleItem item) {
        // קודם מבטלים התראה כדי שלא תצא אחרי מחיקה
        cancelReminder(item.getAlarmId());

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(v -> Toast.makeText(this, "נמחק", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                });
    }

    private void goToMainAndFinish() {
        Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ---- Notifications permission (Android 13+) ----
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

    // ---- Alarm scheduling ----
    private void scheduleReminder(long triggerAtMillis, String title, String details, int alarmId) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);
        intent.putExtra("notifId", alarmId);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    private void cancelReminder(int alarmId) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);
    }
}
