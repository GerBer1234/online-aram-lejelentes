package com.example.emeter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Egy havi emlékeztető értesítést jelenít meg, amely a HomeActivity-re navigál.
 */
public class NotificationHandler {
    private static final String CHANNEL_ID = "reminder_channel";
    public static void showNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "Emlékeztető csatorna",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra("from_notification", true); // jelöljük, hogy értesítésből jön
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // ne töröljön taskot

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("eMeter emlékeztető")
                .setContentText("Ne felejts el új fogyasztási adatot rögzíteni!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                        == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        }
    }
}
