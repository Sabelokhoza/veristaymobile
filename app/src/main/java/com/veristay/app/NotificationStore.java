
package com.veristay.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationStore {

    private static final String PREF_NAME = "veristay_notifications";
    private static final String KEY_LIST  = "notification_list";
    private static final int    MAX_SIZE  = 50;
    private static final Gson   gson      = new Gson();



    public static void save(Context context,
                            String title,
                            String message,
                            String type) {
        List<Models.AppNotification> list = getAll(context);

        // ✅ New way — set fields directly, no constructor
        Models.AppNotification notification = new Models.AppNotification();
        notification.title     = title;
        notification.message   = message;
        notification.type      = type;
        notification.isRead    = false;
        notification.id        = (int) (System.currentTimeMillis() / 1000);
        notification.createdAt = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date());

        // Add to front
        list.add(0, notification);

        // Keep max 50
        if (list.size() > MAX_SIZE) {
            list = list.subList(0, MAX_SIZE);
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    // ── Get all notifications ─────────────────────────────────────

    public static List<Models.AppNotification> getAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<Models.AppNotification>>() {}.getType();
        List<Models.AppNotification> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    // ── Get unread count ──────────────────────────────────────────

    public static int getUnreadCount(Context context) {
        List<Models.AppNotification> list = getAll(context);
        int count = 0;
        for (Models.AppNotification n : list) {
            if (!n.isRead) count++;
        }
        return count;
    }

    // ── Mark all as read ──────────────────────────────────────────

    public static void markAllRead(Context context) {
        List<Models.AppNotification> list = getAll(context);
        for (Models.AppNotification n : list) n.isRead = true;
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    // ── Clear all ─────────────────────────────────────────────────

    public static void clear(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}