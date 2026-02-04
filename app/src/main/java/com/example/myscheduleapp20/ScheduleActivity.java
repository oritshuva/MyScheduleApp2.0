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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ScheduleItem> items = new ArrayList<>();
    private ScheduleAdapter adapter;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> addItemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 🔹 Firestore
        db = FirebaseFirestore.getInstance();

        // 🔹 UI
        Button btnAddItem = findViewById(R.id.btnAddItem);
        RecyclerView recyclerSchedule = findViewById(R.id.recyclerSchedule);

        adapter = new ScheduleAdapter(items);
        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);

        // 🔹 Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        // 🔹 טעינת משימות מ-Firestore
        db.collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    items.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        String details = doc.getString("details");
                        String displayTime = doc.getString("displayTime");

                        if (title == null) title = "";
                        if (details == null) details = "";
                        if (displayTime == null) displayTime = "";

                        items.add(new ScheduleItem(title, displayTime, details));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(Throwable::printStackTrace);

        // 🔹 פתיחת מסך הוספת משימה
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String title = result.getData().getStringExtra("title");
                        String details = result.getData().getStringExtra("details");
                        String displayTime = result.getData().getStringExtra("displayTime");

                        if (title == null) title = "";
                        if (details == null) details = "";
                        if (displayTime == null) displayTime = "";

                        if (!title.trim().isEmpty()) {
                            items.add(new ScheduleItem(title, displayTime, details));
                            adapter.notifyItemInserted(items.size() - 1);
                        }
                    }
                }
        );

        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleItemActivity.class);
            addItemLauncher.launch(intent);
        });
    }
}
