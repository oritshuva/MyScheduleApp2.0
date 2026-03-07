package com.example.myscheduleapp20;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myscheduleapp20.model.ScheduleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private Context context;
    private List<ScheduleItem> items;

    public ScheduleAdapter(Context context, List<ScheduleItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_schedule, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ScheduleItem item = items.get(position);

        holder.tvSubject.setText(item.getSubjectName());

        holder.tvTime.setText(
                item.getStartTime() + " - " + item.getEndTime()
        );

        holder.tvNote.setText(item.getNote());

        holder.btnDelete.setOnClickListener(v -> confirmDelete(item, position));
    }

    private void confirmDelete(ScheduleItem item, int position) {

        new AlertDialog.Builder(context)
                .setTitle("מחיקת שיעור")
                .setMessage("האם למחוק את השיעור?")
                .setPositiveButton("כן", (dialog, which) -> deleteItem(item, position))
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void deleteItem(ScheduleItem item, int position) {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("scheduleItems")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(unused -> {

                    items.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "השיעור נמחק", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "שגיאה במחיקה", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubject;
        TextView tvTime;
        TextView tvNote;

        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNote = itemView.findViewById(R.id.tvNote);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}