package com.example.myscheduleapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
            auth.signOut();
            goToMainAndFinish();
        });

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                    String docId = safeStr(result.getData().getStringExtra("docId"));
                    String title = safeStr(result.getData().getStringExtra("title"));
                    String details = safeStr(result.getData().getStringExtra("details"));
                    String displayTime = safeStr(result.getData().getStringExtra("displayTime"));
                    long triggerAtMillis = result.getData().getLongExtra("triggerAtMillis", -1);

                    if (title.trim().isEmpty() || triggerAtMillis <= 0) {
                        Toast.makeText(this, "נתונים לא תקינים", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> task = new HashMap<>();
                    task.put("title", title);
                    task.put("details", details);
                    task.put("displayTime", displayTime);
                    task.put("time", triggerAtMillis); // number (Long)

                    boolean isEdit = !docId.isEmpty();

                    if (isEdit) {
                        db.collection("users")
                                .document(uid)
                                .collection("tasks")
                                .document(docId)
                                .update(task)
                                .addOnSuccessListener(v -> Toast.makeText(this, "עודכן!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        db.collection("users")
                                .document(uid)
                                .collection("tasks")
                                .add(task)
                                .addOnSuccessListener(docRef -> Toast.makeText(this, "נשמר!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
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
                        long time = (t != null) ? t : 0L;

                        String displayTime = safeStr(doc.getString("displayTime"));
                        if (displayTime.isEmpty()) displayTime = formatDisplayTime(time);

                        items.add(new ScheduleItem(id, title, displayTime, details, time));
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
        intent.putExtra("triggerAtMillis", item.getTime());
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
}
