package com.example.myscheduleapp20;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<ScheduleItem> items;

    public ScheduleAdapter(List<ScheduleItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = items.get(position);

        String title = item.getTitle() != null ? item.getTitle() : "";
        String time = item.getTime() != null ? item.getTime() : "";
        String details = item.getDetails() != null ? item.getDetails() : "";

        holder.txtTitle.setText(title);
        holder.txtTime.setText(time);

        // ✅ חסין: אם txtDetails לא קיים ב-XML, לא נוגעים בו
        if (holder.txtDetails != null) {
            holder.txtDetails.setText(details);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TaskDetailsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("time", time);
            intent.putExtra("details", details);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtTime;
        TextView txtDetails; // יכול להיות null אם לא קיים ב-XML

        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDetails = itemView.findViewById(R.id.txtDetails); // אם אין - יחזור null וזה בסדר
        }
    }
}
