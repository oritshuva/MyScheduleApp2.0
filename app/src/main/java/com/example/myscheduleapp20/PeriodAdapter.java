package com.example.myscheduleapp20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.VH> {

    private final List<ScheduleEntry> items;

    public PeriodAdapter(List<ScheduleEntry> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        ScheduleEntry entry = items.get(position);

        holder.period.setText(String.valueOf(position + 1));

        holder.subject.setText(
                entry.title == null || entry.title.isEmpty()
                        ? "לחץ להוסיף שיעור"
                        : entry.title
        );

        holder.time.setText(entry.details);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView period;
        TextView subject;
        TextView time;

        VH(View itemView) {
            super(itemView);

            period = itemView.findViewById(R.id.tvPeriod);
            subject = itemView.findViewById(R.id.tvSubject);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}