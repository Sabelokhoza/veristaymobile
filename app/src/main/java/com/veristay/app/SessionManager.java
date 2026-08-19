package com.veristay.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME    = "veristay_prefs";
    private static final String KEY_TOKEN    = "token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_FULLNAME = "full_name";
    private static final String KEY_ROLE     = "role";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ── Save after login ──────────────────────────────────────────────

    public void saveSession(String token, String userId,
                            String email, String fullName, String role) {
        editor.putString(KEY_TOKEN,    token);
        editor.putString(KEY_USER_ID,  userId);
        editor.putString(KEY_EMAIL,    email);
        editor.putString(KEY_FULLNAME, fullName);
        editor.putString(KEY_ROLE,     role);
        editor.apply();
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String getToken()    { return prefs.getString(KEY_TOKEN,    null); }
    public String getUserId()   { return prefs.getString(KEY_USER_ID,  null); }
    public String getEmail()    { return prefs.getString(KEY_EMAIL,    null); }
    public String getFullName() { return prefs.getString(KEY_FULLNAME, null); }
    public String getRole()     { return prefs.getString(KEY_ROLE,     null); }
    public String getFcmToken() { return prefs.getString(KEY_FCM_TOKEN, null); }

    public boolean isLoggedIn() { return getToken() != null; }
    // Update isStudent to not rely on role if role is hardcoded
    public boolean isStudent() {
        String role = getRole();
        // Accept "Student" or default if null
        return role == null || "Student".equalsIgnoreCase(role);
    }
    public boolean isLandlord() { return "Landlord".equalsIgnoreCase(getRole()); }
    public boolean isAdmin()    { return "Admin".equalsIgnoreCase(getRole()); }

    public void saveFcmToken(String fcmToken) {
        editor.putString(KEY_FCM_TOKEN, fcmToken).apply();
    }

    // ── Clear on logout ───────────────────────────────────────────────

    public void clearSession() {
        editor.clear().apply();
    }
}