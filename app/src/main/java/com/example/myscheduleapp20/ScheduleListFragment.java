package com.example.myscheduleapp20;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleListFragment extends Fragment {

    private static final String ARG_TYPE = "schedule_type";

    private String scheduleType;

    private RecyclerView recyclerView;
    private TextView tvEmptyDay;

    private ScheduleAdapter adapter;
    private List<ScheduleItem> items = new ArrayList<>();

    public ScheduleListFragment() {
    }

    public static ScheduleListFragment newInstance(String type) {

        ScheduleListFragment fragment = new ScheduleListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_schedule_list,
                container,
                false);

        if (getArguments() != null) {
            scheduleType = getArguments().getString(ARG_TYPE);
        }

        recyclerView = view.findViewById(R.id.recyclerSchedule);
        tvEmptyDay = view.findViewById(R.id.tvEmptyDay);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));

        adapter = new ScheduleAdapter(requireContext(), items);
        recyclerView.setAdapter(adapter);

        loadSchedule();

        return view;
    }

    private void loadSchedule() {

        String uid = FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseFirestore
                .getInstance()
                .collection("tasks")
                .document(uid)
                .collection("userTasks")
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    items.clear();

                    for (QueryDocumentSnapshot doc : value) {

                        ScheduleItem item =
                                doc.toObject(ScheduleItem.class);

                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }

                    Collections.sort(items,
                            new Comparator<ScheduleItem>() {
                                @Override
                                public int compare(
                                        ScheduleItem a,
                                        ScheduleItem b) {

                                    return Integer.compare(
                                            a.getPeriodNumber(),
                                            b.getPeriodNumber());
                                }
                            });

                    adapter.notifyDataSetChanged();

                    if (items.isEmpty()) {

                        tvEmptyDay.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);

                    } else {

                        tvEmptyDay.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                });
    }
}