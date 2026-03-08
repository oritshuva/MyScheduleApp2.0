package com.example.myscheduleapp20;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment {

    private RecyclerView recyclerTasks;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private FirebaseFirestore db;

    public TaskListFragment() {
        super(R.layout.fragment_task_list);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerTasks = view.findViewById(R.id.recyclerTasks);

        recyclerTasks.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        taskList = new ArrayList<>();

        adapter = new TaskAdapter(taskList);

        recyclerTasks.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadTasks();
    }

    private void loadTasks() {

        db.collection("tasks")
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    taskList.clear();

                    for (QueryDocumentSnapshot doc : value) {

                        Task task = doc.toObject(Task.class);

                        taskList.add(task);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}