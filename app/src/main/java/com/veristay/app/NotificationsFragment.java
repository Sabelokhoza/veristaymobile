package com.veristay.app;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.veristay.app.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private SessionManager session;
    private ApiService api;
    private NotificationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        adapter = new NotificationsAdapter(
                this::onMarkAsRead,
                this::onDeleteNotification);

        binding.rvNotifications.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);

        // Clear all button
        binding.btnClearAll.setOnClickListener(v -> deleteAll());

        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh every time screen is opened
        loadNotifications();
    }

    // =============================================
    // Load from backend
    // =============================================

    private void loadNotifications() {
        if (binding == null) return;
        showLoading(true);

        api.getUserNotifications(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<List<Models.AppNotification>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<List<Models.AppNotification>>> call,
                            @NonNull Response<Models.ApiResponse<List<Models.AppNotification>>> response) {

                        if (binding == null) return;
                        showLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {

                            List<Models.AppNotification> notifications =
                                    response.body().data;

                            if (notifications.isEmpty()) {
                                showEmpty(true);
                            } else {
                                showEmpty(false);
                                adapter.setNotifications(notifications);
                                binding.btnClearAll.setVisibility(View.VISIBLE);
                            }

                            // Mark all as read silently
                            markAllReadSilently();

                        } else {
                            showEmpty(true);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<List<Models.AppNotification>>> call,
                            @NonNull Throwable t) {
                        if (binding == null) return;
                        showLoading(false);
                        android.util.Log.e("VeriStay",
                                "Failed to load notifications: " + t.getMessage());
                        // Fall back to local store if offline
                        loadFromLocalStore();
                    }
                });
    }

    // =============================================
    // Mark single as read
    // =============================================

    private void onMarkAsRead(Models.AppNotification notification) {
        if (notification.isRead) return;

        api.markAsRead(notification.id)
                .enqueue(new Callback<Models.ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Response<Models.ApiResponse<Boolean>> response) {
                        // Update locally
                        notification.isRead = true;
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Throwable t) {
                        android.util.Log.e("VeriStay",
                                "Mark read failed: " + t.getMessage());
                    }
                });
    }

    // =============================================
    // Delete single notification
    // =============================================

    private void onDeleteNotification(Models.AppNotification notification) {
        api.deleteNotification(notification.id)
                .enqueue(new Callback<Models.ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Response<Models.ApiResponse<Boolean>> response) {
                        if (response.isSuccessful()) {
                            loadNotifications(); // refresh list
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =============================================
    // Delete all
    // =============================================

    private void deleteAll() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    api.deleteAllNotifications(session.getUserId())
                            .enqueue(new Callback<Models.ApiResponse<Boolean>>() {
                                @Override
                                public void onResponse(
                                        @NonNull Call<Models.ApiResponse<Boolean>> call,
                                        @NonNull Response<Models.ApiResponse<Boolean>> response) {
                                    if (binding == null) return;
                                    adapter.setNotifications(new ArrayList<>());
                                    showEmpty(true);
                                    binding.btnClearAll.setVisibility(View.GONE);
                                    Toast.makeText(getContext(),
                                            "All notifications cleared",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(
                                        @NonNull Call<Models.ApiResponse<Boolean>> call,
                                        @NonNull Throwable t) {
                                    Toast.makeText(getContext(),
                                            "Failed to clear", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =============================================
    // Mark all read silently (no UI update needed)
    // =============================================

    private void markAllReadSilently() {
        api.markAllRead(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Response<Models.ApiResponse<Boolean>> response) {
                        android.util.Log.d("VeriStay", "All notifications marked read");
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Boolean>> call,
                            @NonNull Throwable t) {
                        android.util.Log.e("VeriStay",
                                "Mark all read failed: " + t.getMessage());
                    }
                });
    }

    // =============================================
    // Offline fallback — local store
    // =============================================

    private void loadFromLocalStore() {
        List<Models.AppNotification> local =
                NotificationStore.getAll(requireContext());

        if (local.isEmpty()) {
            showEmpty(true);
        } else {
            showEmpty(false);
            adapter.setNotifications(local);
            binding.btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    // =============================================
    // UI helpers
    // =============================================

    private void showLoading(boolean loading) {
        if (binding == null) return;
        // Optional: add a progress bar to fragment_notifications.xml
        // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean empty) {
        if (binding == null) return;
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) binding.btnClearAll.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}