package com.example.myscheduleapp20;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = taskList.get(position);

        holder.txtTitle.setText(task.getTitle());
        holder.txtDetails.setText(task.getDetails());

        SimpleDateFormat sdf =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        holder.txtTime.setText(
                sdf.format(new Date(task.getTimeInMillis()))
        );

        // במקום עיגול – נשנה צבע כותרת אם עבר הזמן
        if (task.isPast()) {
            holder.txtTitle.setTextColor(Color.GREEN);
        } else {
            holder.txtTitle.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtDetails;
        TextView txtTime;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}