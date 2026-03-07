package com.example.myscheduleapp20;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        // constructor חובה ל-Fragment
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerTasks = view.findViewById(R.id.recyclerTasks);

        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        taskList = new ArrayList<>();

        adapter = new TaskAdapter(taskList);
        recyclerTasks.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {

        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    taskList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        taskList.add(task);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}