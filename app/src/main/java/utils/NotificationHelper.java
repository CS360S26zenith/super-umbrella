package com.example.campuseventstest.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.campuseventstest.R;

/**
 * Shows lightweight tray (status bar) alerts for RSVP reminders and similar flows.
 * Requires {@link android.Manifest.permission#POST_NOTIFICATIONS} on Android 13+.
 */
public final class NotificationHelper {

    private static final String CHANNEL_ID = "campus_events_general";

    private NotificationHelper() {
    }

    /**
     * Ensures a notification channel exists on Android O+.
     */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Campus Events",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("RSVP confirmations and event reminders");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Posts a standard notification that appears in the system shade.
     */
    public static void showTrayNotification(Context context, String title, String body) {
        ensureChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }
}
