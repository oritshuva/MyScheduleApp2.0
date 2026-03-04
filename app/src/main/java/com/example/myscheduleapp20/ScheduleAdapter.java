package com.example.myscheduleapp20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {

    public interface OnItemActionListener {
        void onClick(ScheduleItem item);
        void onLongClick(ScheduleItem item);
    }

    private final List<ScheduleItem> items;
    private final OnItemActionListener listener;

    public ScheduleAdapter(List<ScheduleItem> items,
                           OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent,
                                 int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false); // ✅ תיקון כאן

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder,
                                 int position) {

        ScheduleItem item = items.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.txtTime.setText(item.getDisplayTime());
        holder.txtDetails.setText(item.getDetails());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtTime;
        TextView txtDetails;

        VH(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDetails = itemView.findViewById(R.id.txtDetails);
        }
    }
}