package com.example.emeter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Havi emlékeztető értesítés kezelésére szolgáló BroadcastReceiver.
 * Megjelenít egy értesítést, és újraütemezi magát a következő hónapra.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Értesítés megjelenítése
        NotificationHandler.showNotification(context);

        // Következő hónap beütemezése
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                123,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        nextMonth.set(Calendar.HOUR_OF_DAY, 8);
        nextMonth.set(Calendar.MINUTE, 0);
        nextMonth.set(Calendar.SECOND, 0);

        // Verziótól függő újraütemezés
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMonth.getTimeInMillis(),
                        pendingIntent);
            }
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextMonth.getTimeInMillis(),
                    pendingIntent);
        }
    }
}
