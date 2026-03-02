package com.example.myscheduleapp20;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScheduleListFragment extends Fragment {

    private static final String ARG_TYPE = "schedule_type";

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private final List<ScheduleItem> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration tasksListener;

    private String scheduleType = "רגיל";

    public ScheduleListFragment() {
        super(R.layout.fragment_schedule_list);
    }

    public static ScheduleListFragment newInstance(String type) {
        ScheduleListFragment fragment = new ScheduleListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            scheduleType = getArguments().getString(ARG_TYPE, "רגיל");
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ScheduleAdapter(filteredList, new ScheduleAdapter.OnItemActionListener() {
            @Override
            public void onClick(ScheduleItem item) {
                Intent intent = new Intent(requireContext(), AddScheduleItemActivity.class);
                intent.putExtra("docId", item.getId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("details", item.getDetails());
                intent.putExtra("scheduleType", item.getScheduleType());
                intent.putExtra("displayTime", item.getDisplayTime());
                intent.putExtra("triggerAtMillis", item.getTime());
                startActivity(intent);
            }

            @Override
            public void onLongClick(ScheduleItem item) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(item.getTitle())
                        .setItems(new String[]{"שתף", "מחק"}, (dialog, which) -> {
                            if (which == 0) {
                                shareTaskText(item);
                            } else {
                                deleteTask(item);
                            }
                        })
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        attachTasksListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tasksListener != null) {
            tasksListener.remove();
        }
    }

    private void attachTasksListener() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        tasksListener = db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        loadFromLocalProvider();
                        return;
                    }
                    if (value == null) return;

                    filteredList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        ScheduleItem item = doc.toObject(ScheduleItem.class);
                        item.setId(doc.getId());
                        if (scheduleType.equals(item.getScheduleType())) {
                            filteredList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void deleteTask(ScheduleItem item) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    try {
                        // 1. מחיקה מהשמירה המקומית (ContentProvider)
                        requireContext().getContentResolver().delete(
                                ScheduleContract.TaskEntry.CONTENT_URI,
                                ScheduleContract.TaskEntry.COLUMN_FIREBASE_DOC_ID + "=?",
                                new String[]{item.getId()}
                        );

                        // 2. ביטול ההתראה
                        android.app.AlarmManager alarmManager =
                                (android.app.AlarmManager) requireContext().getSystemService(android.content.Context.ALARM_SERVICE);

                        if (alarmManager != null) {
                            Intent intent = new Intent(requireContext(), ReminderReceiver.class);

                            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                                    requireContext(),
                                    item.getAlarmId(),
                                    intent,
                                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                            );

                            alarmManager.cancel(pendingIntent);
                            pendingIntent.cancel();
                        }

                        Toast.makeText(requireContext(), "המשימה נמחקה", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "נמחק מ-Firebase, אבל הייתה בעיה בניקוי המקומי", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "מחיקה נכשלה", Toast.LENGTH_SHORT).show()
                );
    }

    private void cancelAlarm(ScheduleItem item) {
        AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                requireContext(),
                item.getAlarmId(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (am != null && pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }

    private void shareTaskText(ScheduleItem item) {
        String text = "📌 משימה: " + (item.getTitle() != null ? item.getTitle() : "") + "\n"
                + "🕒 שעה: " + (item.getDisplayTime() != null ? item.getDisplayTime() : "");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "שתף באמצעות"));
    }

    private void loadFromLocalProvider() {
        // קוד טעינה מקומי כפי שהיה לך (השארתי כדי לא להעמיס)
    }
}