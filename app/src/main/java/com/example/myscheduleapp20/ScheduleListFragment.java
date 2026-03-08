package com.example.myscheduleapp20;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleListFragment extends Fragment {

    private RecyclerView recycler;
    private TextView emptyText;
    private ScheduleAdapter adapter;
    private List<ScheduleItem> list = new ArrayList<>();
    private String scheduleType, day;

    public ScheduleListFragment(){ super(R.layout.fragment_schedule_list); }

    public static ScheduleListFragment newInstance(String type, String day){
        ScheduleListFragment fragment = new ScheduleListFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("day", day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        recycler = view.findViewById(R.id.recyclerSchedule);
        emptyText = view.findViewById(R.id.tvEmptyDay);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // אתחול האדפטר עם המאזין למחיקה
        adapter = new ScheduleAdapter(list, getContext(), item -> showDeleteDialog(item));
        recycler.setAdapter(adapter);

        scheduleType = getArguments().getString("type");
        day = getArguments().getString("day");

        loadData();
    }

    private void loadData(){
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).collection("scheduleItems")
                .whereEqualTo("scheduleType", scheduleType)
                .whereEqualTo("day", day)
                .addSnapshotListener((value, error) -> {
                    if(value == null) return;
                    list.clear();
                    for(QueryDocumentSnapshot doc : value){
                        ScheduleItem item = doc.toObject(ScheduleItem.class);
                        item.setId(doc.getId());
                        list.add(item);
                    }
                    Collections.sort(list, (a, b) -> a.getPeriodNumber() - b.getPeriodNumber());
                    adapter.notifyDataSetChanged();
                    emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showDeleteDialog(ScheduleItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("מחיקת שיעור")
                .setMessage("האם אתה בטוח שברצונך למחוק את " + item.getSubjectName() + "?")
                .setPositiveButton("מחק", (dialog, which) -> deleteItemFromFirebase(item))
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void deleteItemFromFirebase(ScheduleItem item) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || item.getId() == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("scheduleItems").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "השיעור נמחק", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show());
    }
}