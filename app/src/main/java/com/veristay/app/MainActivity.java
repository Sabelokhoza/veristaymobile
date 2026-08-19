package com.veristay.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.veristay.app.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "VeriStay";

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityMainBinding binding;
    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        api = RetrofitClient.getInstance(session).getApiService();

        // Request notification permission on Android 13+
        requestNotificationPermission();

        // Setup notification channels
        new NotificationHelper(this).createChannels();

        // Setup FCM — get token and send to backend
        setupFCM();

        // Schedule payment reminders every 12 hours
        schedulePaymentReminders();

        // Setup bottom navigation
        binding.bottomNav.setOnNavigationItemSelectedListener(this);

        // Check if launched from notification tap
        handleNotificationIntent(getIntent());

        // Load default fragment
        loadFragment(new HomeFragment());
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
    }

    // ============================================================
    // ANDROID NOTIFICATION PERMISSION
    // ============================================================

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                Log.d(
                        TAG,
                        "Requesting notification permission..."
                );

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );

            } else {

                Log.d(
                        TAG,
                        "Notification permission already granted ✅"
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d(
                        TAG,
                        "Notification permission granted ✅"
                );

            } else {

                Log.e(
                        TAG,
                        "Notification permission denied ❌"
                );
            }
        }
    }

    // ============================================================
    // FCM
    // ============================================================

    private void setupFCM() {

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Log.e(
                                TAG,
                                "FCM token failed: " +
                                        (task.getException() != null
                                                ? task.getException().getMessage()
                                                : "unknown")
                        );

                        return;
                    }

                    String fcmToken = task.getResult();

                    Log.d(
                            TAG,
                            "FCM Token: " + fcmToken
                    );

                    // Save locally
                    session.saveFcmToken(fcmToken);

                    // Send token to backend
                    String userId = session.getUserId();

                    if (userId != null && !userId.isEmpty()) {

                        api.updateFcmToken(
                                new ApiService.UpdateFcmTokenDto(
                                        userId,
                                        fcmToken
                                )
                        ).enqueue(
                                new Callback<Models.ApiResponse<Boolean>>() {

                                    @Override
                                    public void onResponse(
                                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                                            @NonNull Response<Models.ApiResponse<Boolean>> response) {

                                        if (response.isSuccessful()) {

                                            Log.d(
                                                    TAG,
                                                    "FCM token sent to backend ✅"
                                            );

                                        } else {

                                            Log.e(
                                                    TAG,
                                                    "FCM token send failed: "
                                                            + response.code()
                                            );
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                                            @NonNull Throwable t) {

                                        Log.e(
                                                TAG,
                                                "FCM token network error: "
                                                        + t.getMessage()
                                        );
                                    }
                                }
                        );
                    }

                    // Subscribe to role topic
                    String role = session.getRole();

                    if (role != null && !role.isEmpty()) {

                        String topic = role.toLowerCase();

                        FirebaseMessaging.getInstance()
                                .subscribeToTopic(topic)
                                .addOnCompleteListener(topicTask -> {

                                    if (topicTask.isSuccessful()) {

                                        Log.d(
                                                TAG,
                                                "Subscribed to topic: "
                                                        + topic + " ✅"
                                        );

                                    } else {

                                        Log.e(
                                                TAG,
                                                "Failed to subscribe to topic: "
                                                        + topic,
                                                topicTask.getException()
                                        );
                                    }
                                });
                    }
                });
    }

    // ============================================================
    // NOTIFICATION TAP
    // ============================================================

    private void handleNotificationIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        String type =
                intent.getStringExtra("notification_type");

        if (type == null) {
            return;
        }

        switch (type) {

            case "payment":

                binding.bottomNav.setSelectedItemId(
                        R.id.nav_payments
                );

                loadFragment(
                        new PaymentsFragment()
                );

                break;

            case "maintenance":

                binding.bottomNav.setSelectedItemId(
                        R.id.nav_maintenance
                );

                loadFragment(
                        new MaintenanceFragment()
                );

                break;

            case "announcement":

                binding.bottomNav.setSelectedItemId(
                        R.id.nav_announcements
                );

                loadFragment(
                        new AnnouncementsFragment()
                );

                break;

            case "application":

            default:

                binding.bottomNav.setSelectedItemId(
                        R.id.nav_home
                );

                loadFragment(
                        new HomeFragment()
                );

                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        setIntent(intent);

        handleNotificationIntent(intent);
    }

    // ============================================================
    // BOTTOM NAVIGATION
    // ============================================================

    @Override
    public boolean onNavigationItemSelected(
            @NonNull MenuItem item) {

        Fragment fragment = null;

        int id = item.getItemId();

        if (id == R.id.nav_home) {

            fragment = new HomeFragment();

        } else if (id == R.id.nav_applications) {

            fragment = new ApplicationsFragment();

        } else if (id == R.id.nav_payments) {

            fragment = new PaymentsFragment();

        } else if (id == R.id.nav_maintenance) {

            fragment = new MaintenanceFragment();

        } else if (id == R.id.nav_announcements) {

            fragment = new AnnouncementsFragment();
        }

        if (fragment != null) {

            loadFragment(fragment);

            return true;
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        fragment
                )
                .commit();
    }

    // ============================================================
    // PAYMENT REMINDERS
    // ============================================================

    private void schedulePaymentReminders() {

        if (!session.isStudent()) {
            return;
        }

        PeriodicWorkRequest reminderWork =
                new PeriodicWorkRequest.Builder(
                        PaymentReminderWorker.class,
                        12,
                        TimeUnit.HOURS
                ).build();

        WorkManager
                .getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(
                        "payment_reminder",
                        ExistingPeriodicWorkPolicy.KEEP,
                        reminderWork
                );
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    public void logout() {

        WorkManager
                .getInstance(getApplicationContext())
                .cancelAllWork();

        session.clearSession();

        startActivity(
                new Intent(
                        this,
                        LoginActivity.class
                )
        );

        finish();
    }
}