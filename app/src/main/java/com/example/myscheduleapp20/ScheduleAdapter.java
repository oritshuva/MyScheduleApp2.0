package com.example.myscheduleapp20;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {

    public interface OnItemActionListener {
        void onClick(ScheduleItem item);
        void onLongClick(ScheduleItem item);
    }

    private final List<ScheduleItem> items;
    private final OnItemActionListener listener;

    public ScheduleAdapter(List<ScheduleItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // שים לב: שיניתי ל-item_task כי זה שם הקובץ שיצרנו לעיצוב השורה
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ScheduleItem item = items.get(position);

        h.txtTitle.setText(item.getTitle());
        h.txtTime.setText(item.getDisplayTime());
        h.txtDetails.setText(item.getDetails());

        // לוגיקת העיגול: ירוק אם הזמן עבר, אפור אם לא
        if (item.isPast()) {
            h.viewStatusCircle.getBackground().setTint(Color.GREEN);
        } else {
            h.viewStatusCircle.getBackground().setTint(Color.GRAY);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder  {
        TextView txtTitle, txtTime, txtDetails;
        View viewStatusCircle;

        VH(@NonNull View itemView) {
            super(itemView);
            // קישור לרכיבים לפי ה-ID שקיים ב-item_task.xml
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtTime = itemView.findViewById(R.id.txtTaskTime);
            txtDetails = itemView.findViewById(R.id.txtTaskDetails);
            viewStatusCircle = itemView.findViewById(R.id.viewStatusCircle);
        }
    }
}