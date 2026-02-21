package com.example.myscheduleapp20;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {

    public interface OnItemActionListener {
        void onClick(ScheduleItem item);     // עריכה/הוספת הערה (Image 15-16)
        void onLongClick(ScheduleItem item); // מחיקה
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ScheduleItem item = items.get(position);

        // הצגת נתוני השיעור/אירוע
        h.txtTitle.setText(item.getTitle());
        h.txtTime.setText(item.getDisplayTime());
        h.txtDetails.setText(item.getDetails());

        // סטטוס אירוע (עיגול אפור הופך לירוק כשעובר הזמן - דרישה מהאפיון)
        if (item.isPast()) {
            h.viewStatusCircle.getBackground().setTint(Color.parseColor("#4CAF50")); // ירוק
        } else {
            h.viewStatusCircle.getBackground().setTint(Color.parseColor("#808080")); // אפור
        }

        // כפתור "הזמן חבר" (שיתוף - Image 1)
        h.btnShareTask.setOnClickListener(v -> {
            String inviteMsg = "היי! בוא ניפגש בשיעור/אירוע: " + item.getTitle() +
                    "\nזמן: " + item.getDisplayTime() +
                    "\nהערות: " + item.getDetails();

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMsg);
            v.getContext().startActivity(Intent.createChooser(sendIntent, "הזמן חבר באמצעות..."));
        });

        // לחיצה לפתיחת מסך הערה/עריכה (כפי שמופיע בתמונות 15-16)
        h.itemView.setOnClickListener(v -> listener.onClick(item));

        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtTime, txtDetails;
        View viewStatusCircle;
        ImageButton btnShareTask;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtTime = itemView.findViewById(R.id.txtTaskTime);
            txtDetails = itemView.findViewById(R.id.txtTaskDetails);
            viewStatusCircle = itemView.findViewById(R.id.viewStatusCircle);
            btnShareTask = itemView.findViewById(R.id.btnShareTask);
        }
    }

}