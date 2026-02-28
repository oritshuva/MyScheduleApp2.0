package com.example.myscheduleapp20;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
            String arg = getArguments().getString(ARG_TYPE);
            if (arg != null && !arg.trim().isEmpty()) {
                scheduleType = arg;
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
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
            tasksListener = null;
        }
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
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "בעיה ב-Firebase, טוען מקומי...", Toast.LENGTH_SHORT).show();
                        }
                        loadFromLocalProvider();
                        return;
                    }

                    if (value == null) return;

                    filteredList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            ScheduleItem item = doc.toObject(ScheduleItem.class);
                            if (item == null) continue;

                            item.setId(doc.getId());

                            String type = item.getScheduleType();
                            if (type == null || type.trim().isEmpty()) {
                                type = "רגיל";
                                item.setScheduleType(type);
                            }

                            if (scheduleType.equals(type)) {
                                filteredList.add(item);
                            }
                        } catch (Exception ignored) {
                            // מדלגים על מסמך לא תקין
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void loadFromLocalProvider() {
        try {
            Cursor cursor = requireContext().getContentResolver().query(
                    ScheduleContract.TaskEntry.CONTENT_URI,
                    null,
                    ScheduleContract.TaskEntry.COLUMN_SCHEDULE_TYPE + "=?",
                    new String[]{scheduleType},
                    ScheduleContract.TaskEntry.COLUMN_TIME_MILLIS + " ASC"
            );

            if (cursor == null) return;

            filteredList.clear();

            int idxFirebaseId = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_FIREBASE_DOC_ID);
            int idxTitle = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_TITLE);
            int idxDisplayTime = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_DISPLAY_TIME);
            int idxDetails = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_DETAILS);
            int idxTimeMillis = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_TIME_MILLIS);
            int idxAlarmId = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_ALARM_ID);
            int idxType = cursor.getColumnIndex(ScheduleContract.TaskEntry.COLUMN_SCHEDULE_TYPE);

            while (cursor.moveToNext()) {
                String firebaseId = (idxFirebaseId >= 0) ? cursor.getString(idxFirebaseId) : "";
                String title = (idxTitle >= 0) ? cursor.getString(idxTitle) : "";
                String displayTime = (idxDisplayTime >= 0) ? cursor.getString(idxDisplayTime) : "";
                String details = (idxDetails >= 0) ? cursor.getString(idxDetails) : "";
                long timeMillis = (idxTimeMillis >= 0) ? cursor.getLong(idxTimeMillis) : 0L;
                int alarmId = (idxAlarmId >= 0) ? cursor.getInt(idxAlarmId) : 0;
                String type = (idxType >= 0) ? cursor.getString(idxType) : scheduleType;

                ScheduleItem item = new ScheduleItem(title, displayTime, details, timeMillis, alarmId, type);
                item.setId(firebaseId);

                filteredList.add(item);
            }

            cursor.close();

            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "נטען מהשמירה המקומית", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "שגיאה בטעינה מקומית", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTask(ScheduleItem item) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("tasks")
                .document(uid)
                .collection("userTasks")
                .document(item.getId())
                .delete()
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "מחיקה נכשלה", Toast.LENGTH_SHORT).show()
                );
    }

    private void shareTaskText(ScheduleItem item) {
        String text = "📌 משימה: " + safe(item.getTitle()) + "\n"
                + "🕒 שעה: " + safe(item.getDisplayTime()) + "\n"
                + "📂 סוג: " + safe(item.getScheduleType()) + "\n"
                + "📝 פירוט: " + safe(item.getDetails());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "שיתוף משימה");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(shareIntent, "שתף באמצעות"));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}