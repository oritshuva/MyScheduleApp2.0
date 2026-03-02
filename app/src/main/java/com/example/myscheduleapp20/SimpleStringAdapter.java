package com.example.myscheduleapp20;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleStringAdapter
        extends RecyclerView.Adapter<SimpleStringAdapter.ViewHolder> {

    public interface Listener {
        void onToggleDone(ScheduleEntry entry);
    }

    private final List<ScheduleEntry> items;
    private final Listener listener;

    public SimpleStringAdapter(List<ScheduleEntry> items,
                               Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void notifyDataChanged() {
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_multiple_choice,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            ViewHolder holder,
            int position) {

        ScheduleEntry entry = items.get(position);

        holder.textView.setText(entry.title);

        if (ScheduleEntry.TYPE_TASK.equals(entry.type)) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(entry.done);

            holder.textView.setTextColor(
                    entry.done ? Color.GRAY : Color.BLACK
            );

            holder.checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        entry.done = isChecked;
                        listener.onToggleDone(entry);
                    });

        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.textView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView textView;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(
                    android.R.id.text1
            );

            checkBox = itemView.findViewById(
                    android.R.id.checkbox
            );
        }
    }
}