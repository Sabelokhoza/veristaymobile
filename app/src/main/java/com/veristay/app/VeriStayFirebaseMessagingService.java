package com.veristay.app;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class VeriStayFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "VeriStayFCM";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        SessionManager session = new SessionManager(getApplicationContext());
        session.saveFcmToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        String title   = "VeriStay";
        String message = "You have a new notification";
        String type    = "general";

        // ── Data payload (sent from backend) ──────────────────────
        if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            type    = data.containsKey("type")    ? data.get("type")    : "general";
            title   = data.containsKey("title")   ? data.get("title")   : title;
            message = data.containsKey("message") ? data.get("message") : message;

            // ── Notification payload ───────────────────────────────────
        } else if (remoteMessage.getNotification() != null) {
            String t = remoteMessage.getNotification().getTitle();
            String m = remoteMessage.getNotification().getBody();
            if (t != null) title   = t;
            if (m != null) message = m;
            type = inferType(title);
        }

        Log.d(TAG, "Type: " + type + " | Title: " + title);

        // ✅ Save to local store — shows in notifications screen
        NotificationStore.save(getApplicationContext(), title, message, type);

        // ✅ Show system push notification
        NotificationHelper helper =
                new NotificationHelper(getApplicationContext());
        int id = new Random().nextInt(10000);
        dispatchNotification(helper, type, title, message, id);

        NotificationStore.save(getApplicationContext(), title, message, type);

        NotificationHelper helper_ = new NotificationHelper(getApplicationContext());
        int id_ = new Random().nextInt(10000);
        dispatchNotification(helper_, type, title, message, id_);

    }

    // ── Infer type from title keywords ────────────────────────────

    private String inferType(String title) {
        if (title == null) return "general";
        String lower = title.toLowerCase();
        if (lower.contains("application")
                || lower.contains("approved")
                || lower.contains("rejected")
                || lower.contains("offer"))
            return "application";
        if (lower.contains("payment")
                || lower.contains("rent")
                || lower.contains("overdue"))
            return "payment";
        if (lower.contains("maintenance")
                || lower.contains("repair"))
            return "maintenance";
        if (lower.contains("announcement")
                || lower.contains("notice"))
            return "announcement";
        return "general";
    }

    // ── Route to correct notification channel ─────────────────────

    private void dispatchNotification(NotificationHelper helper,
                                      String type,
                                      String title,
                                      String message,
                                      int id) {
        switch (type) {
            case "application":
                helper.showApplicationNotification(title, message, id);
                break;
            case "payment":
                helper.showPaymentNotification(title, message, id);
                break;
            case "maintenance":
                helper.showMaintenanceNotification(title, message, id);
                break;
            case "announcement":
                helper.showAnnouncementNotification(title, message, id);
                break;
            default:
                helper.showGeneralNotification(title, message, id);
        }
    }
}