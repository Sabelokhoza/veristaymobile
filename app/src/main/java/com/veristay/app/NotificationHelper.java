package com.veristay.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String TAG = "VeriStayNotification";

    // Channel IDs
    public static final String CHANNEL_APPLICATIONS = "veristay_applications";
    public static final String CHANNEL_PAYMENTS = "veristay_payments";
    public static final String CHANNEL_MAINTENANCE = "veristay_maintenance";
    public static final String CHANNEL_ANNOUNCEMENTS = "veristay_announcements";
    public static final String CHANNEL_GENERAL = "veristay_main";

    private final Context context;
    private final NotificationManager manager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();

        this.manager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannels();
    }

    // ============================================================
    // CREATE NOTIFICATION CHANNELS
    // ============================================================

    public void createChannels() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        createChannel(
                CHANNEL_APPLICATIONS,
                "Application Updates",
                "Notifications for application status changes",
                NotificationManager.IMPORTANCE_HIGH
        );

        createChannel(
                CHANNEL_PAYMENTS,
                "Payment Reminders",
                "Rent payment reminders and confirmations",
                NotificationManager.IMPORTANCE_HIGH
        );

        createChannel(
                CHANNEL_MAINTENANCE,
                "Maintenance Updates",
                "Updates about maintenance requests",
                NotificationManager.IMPORTANCE_HIGH
        );

        createChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Announcements",
                "Announcements from landlords and VeriStay",
                NotificationManager.IMPORTANCE_HIGH
        );

        createChannel(
                CHANNEL_GENERAL,
                "General",
                "General VeriStay notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
    }

    private void createChannel(
            String id,
            String name,
            String description,
            int importance) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            id,
                            name,
                            importance
                    );

            channel.setDescription(description);
            channel.enableVibration(true);

            Uri soundUri =
                    RingtoneManager.getDefaultUri(
                            RingtoneManager.TYPE_NOTIFICATION
                    );

            android.media.AudioAttributes audioAttributes =
                    new android.media.AudioAttributes.Builder()
                            .setUsage(
                                    android.media.AudioAttributes.USAGE_NOTIFICATION
                            )
                            .build();

            channel.setSound(soundUri, audioAttributes);

            manager.createNotificationChannel(channel);

            Log.d(
                    TAG,
                    "Notification channel created: " + id
            );
        }
    }

    // ============================================================
    // APPLICATION NOTIFICATION
    // ============================================================

    public void showApplicationNotification(
            String title,
            String message,
            int notificationId) {

        showNotification(
                CHANNEL_APPLICATIONS,
                title,
                message,
                notificationId,
                "application"
        );
    }

    // ============================================================
    // PAYMENT NOTIFICATION
    // ============================================================

    public void showPaymentNotification(
            String title,
            String message,
            int notificationId) {

        showNotification(
                CHANNEL_PAYMENTS,
                title,
                message,
                notificationId,
                "payment"
        );
    }

    // ============================================================
    // MAINTENANCE NOTIFICATION
    // ============================================================

    public void showMaintenanceNotification(
            String title,
            String message,
            int notificationId) {

        showNotification(
                CHANNEL_MAINTENANCE,
                title,
                message,
                notificationId,
                "maintenance"
        );
    }

    // ============================================================
    // ANNOUNCEMENT NOTIFICATION
    // ============================================================

    public void showAnnouncementNotification(
            String title,
            String message,
            int notificationId) {

        showNotification(
                CHANNEL_ANNOUNCEMENTS,
                title,
                message,
                notificationId,
                "announcement"
        );
    }

    // ============================================================
    // GENERAL NOTIFICATION
    // ============================================================

    public void showGeneralNotification(
            String title,
            String message,
            int notificationId) {

        showNotification(
                CHANNEL_GENERAL,
                title,
                message,
                notificationId,
                "general"
        );
    }

    // ============================================================
    // SHOW NOTIFICATION
    // ============================================================

    private void showNotification(
            String channelId,
            String title,
            String message,
            int id,
            String type) {

        Log.d(
                TAG,
                "Preparing notification: " +
                        "type=" + type +
                        ", title=" + title +
                        ", message=" + message +
                        ", id=" + id
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "❌ POST_NOTIFICATIONS permission NOT granted");
                return;
            }

            Log.d(TAG, "✅ POST_NOTIFICATIONS permission granted");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(channelId);

            if (channel != null) {
                Log.d(TAG,
                        "Channel=" + channelId +
                                " importance=" + channel.getImportance() +
                                " blocked=" + (channel.getImportance()
                                == NotificationManager.IMPORTANCE_NONE));
            }
        }
        // --------------------------------------------------------
        // Intent when notification is clicked
        // --------------------------------------------------------

        Intent intent =
                new Intent(context, MainActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        intent.putExtra(
                "notification_type",
                type
        );

        intent.putExtra(
                "notification_title",
                title
        );

        intent.putExtra(
                "notification_message",
                message
        );

        // --------------------------------------------------------
        // PendingIntent
        // --------------------------------------------------------

        int flags =
                PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        id,
                        intent,
                        flags
                );

        // --------------------------------------------------------
        // Notification sound
        // --------------------------------------------------------

        Uri soundUri =
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                );

        // --------------------------------------------------------
        // Notification builder
        // --------------------------------------------------------

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        channelId
                )
                        .setSmallIcon(
                                getIconForType(type)
                        )
                        .setContentTitle(
                                title != null
                                        ? title
                                        : "VeriStay"
                        )
                        .setContentText(
                                message != null
                                        ? message
                                        : "You have a new notification"
                        )
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(
                                                message != null
                                                        ? message
                                                        : ""
                                        )
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_MESSAGE
                        )
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setVibrate(
                                new long[]{
                                        0,
                                        500,
                                        250,
                                        500
                                }
                        )
                        .setContentIntent(
                                pendingIntent
                        );

        // --------------------------------------------------------
        // Display notification
        // --------------------------------------------------------

        try {

            manager.notify(
                    id,
                    builder.build()
            );

            Log.d(
                    TAG,
                    "✅ Notification displayed successfully. " +
                            "ID=" + id +
                            ", Channel=" + channelId
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "❌ Failed to display notification",
                    e
            );
        }
    }

    // ============================================================
    // ICON BY NOTIFICATION TYPE
    // ============================================================

    public static int getIconForType(String type) {

        if (type == null) {
            return R.drawable.ic_notification;
        }

        switch (type.toLowerCase()) {

            case "application":
                return R.drawable.ic_application;

            case "payment":
                return R.drawable.ic_payment;

            case "maintenance":
                return R.drawable.ic_maintenance;

            case "announcement":
                return R.drawable.ic_announcement;

            case "general":
            default:
                return R.drawable.ic_notification;
        }
    }
}