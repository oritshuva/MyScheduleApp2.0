package com.example.myscheduleapp20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    private final List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        Task task = tasks.get(position);

        holder.radioDone.setOnCheckedChangeListener(null);

        holder.txtTitle.setText(task.getTitle());

        SimpleDateFormat fmt =
                new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

        holder.txtTime.setText(
                fmt.format(new Date(task.getAlarmTime()))
        );

        holder.radioDone.setChecked(task.isDone());

        holder.radioDone.setOnCheckedChangeListener((btn, checked) -> {
            task.setDone(checked);
            holder.txtTitle.setAlpha(checked ? 0.5f : 1f);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        RadioButton radioDone;
        TextView txtTitle;
        TextView txtTime;

        VH(@NonNull View itemView) {
            super(itemView);
            radioDone = itemView.findViewById(R.id.radioDone);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}