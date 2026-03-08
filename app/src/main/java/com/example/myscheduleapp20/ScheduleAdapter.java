package com.example.myscheduleapp20;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myscheduleapp20.model.ScheduleItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder>{

    private List<ScheduleItem> items;
    private Context context;
    private OnItemClickListener listener;

    // Interface חדש לטיפול בלחיצות
    public interface OnItemClickListener {
        void onItemLongClick(ScheduleItem item);
    }

    public ScheduleAdapter(List<ScheduleItem> items, Context context, OnItemClickListener listener){
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        ScheduleItem item = items.get(position);

        holder.period.setText(String.valueOf(item.getPeriodNumber()));
        holder.subject.setText(item.getSubjectName());
        holder.time.setText(item.getStartTime() + " - " + item.getEndTime());

        // לוגיקת צבע העיגול (לפי המסמך)
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if(item.getEndTime() != null && item.getEndTime().compareTo(currentTime) < 0){
            holder.status.setBackgroundResource(R.drawable.circle_green);
        } else {
            holder.status.setBackgroundResource(R.drawable.circle_gray);
        }

        // לחיצה רגילה - מציג את ההערה (Image 15-16 במסמך)
        holder.itemView.setOnClickListener(v -> {
            String note = (item.getNote() != null && !item.getNote().isEmpty()) ? item.getNote() : "אין הערות לשיעור זה";
            new AlertDialog.Builder(context)
                    .setTitle("הערה לשיעור " + item.getSubjectName())
                    .setMessage(note)
                    .setPositiveButton("סגור", null)
                    .show();
        });

        // לחיצה ארוכה - קורא לפונקציית המחיקה ב-Fragment
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount(){ return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView period, subject, time;
        View status;

        public ViewHolder(View itemView){
            super(itemView);
            period = itemView.findViewById(R.id.tvPeriod);
            subject = itemView.findViewById(R.id.tvSubject);
            time = itemView.findViewById(R.id.tvTime);
            status = itemView.findViewById(R.id.viewStatus);
        }
    }
}