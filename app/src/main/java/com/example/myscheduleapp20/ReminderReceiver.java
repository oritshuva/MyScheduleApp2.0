package com.example.myscheduleapp20;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "reminders_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String title = (intent != null) ? intent.getStringExtra("title") : null;
            String details = (intent != null) ? intent.getStringExtra("details") : null;
            int notifId = (intent != null) ? intent.getIntExtra("notifId", 1) : 1;

            if (title == null || title.trim().isEmpty()) title = "תזכורת";
            if (details == null) details = "";

            // Android 13+: אם אין הרשאה -> לא שולחים (ולא קורסים)
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            createChannelIfNeeded(context);

            Intent openIntent = new Intent(context, ScheduleActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    notifId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(details.isEmpty() ? "יש לך משימה עכשיו" : details)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(details.isEmpty() ? "יש לך משימה עכשיו" : details))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);

            NotificationManagerCompat.from(context).notify(notifId, builder.build());

        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Task reminders");
                nm.createNotificationChannel(channel);
            }
        }
    }
}
