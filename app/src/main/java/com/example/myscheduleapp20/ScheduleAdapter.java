package com.example.myscheduleapp20;

import android.content.Intent;
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

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h,
                                 int position) {

        ScheduleItem item = items.get(position);

        h.txtTitle.setText(item.getTitle());
        h.txtTime.setText(item.getDisplayTime());
        h.txtDetails.setText(item.getDetails());

        h.itemView.setOnClickListener(
                v -> listener.onClick(item));

        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtTime;
        TextView txtDetails;

        VH(@NonNull View itemView) {
            super(itemView);

            txtTitle =
                    itemView.findViewById(R.id.txtTitle);

            txtTime =
                    itemView.findViewById(R.id.txtTime);

            txtDetails =
                    itemView.findViewById(R.id.txtDetails);
        }
    }
}