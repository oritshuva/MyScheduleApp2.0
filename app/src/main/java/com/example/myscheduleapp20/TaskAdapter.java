package com.example.myscheduleapp20;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.txtTitle.setText(task.getTitle());
        holder.txtDetails.setText(task.getDetails());

        // הצגת השעה
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        holder.txtTime.setText(sdf.format(new java.util.Date(task.getTimeInMillis())));

        // צביעת העיגול: ירוק אם עבר הזמן, אפור אם לא
        if (task.isPast()) {
            holder.viewStatus.getBackground().setTint(Color.GREEN);
        } else {
            holder.viewStatus.getBackground().setTint(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDetails, txtTime;
        View viewStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtDetails = itemView.findViewById(R.id.txtTaskDetails);
            txtTime = itemView.findViewById(R.id.txtTaskTime);
            viewStatus = itemView.findViewById(R.id.viewStatusCircle);
        }
    }
}