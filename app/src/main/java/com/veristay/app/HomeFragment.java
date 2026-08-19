package com.veristay.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

import com.veristay.app.R;
import com.veristay.app.databinding.FragmentHomeBinding;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager session;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        // ── Greeting ──────────────────────────────────────────────
        String name = session.getFullName();
        if (name != null && !name.isEmpty()) {
            String firstName = name.contains(" ")
                    ? name.split(" ")[0] : name;
            binding.tvGreeting.setText("Welcome back, " + firstName + " 👋");
        } else {
            binding.tvGreeting.setText("Welcome back 👋");
        }

        String role = session.getRole();
        binding.tvRole.setText(role != null ? role : "Student");

        // ── Logout button ─────────────────────────────────────────
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());

        // ── Swipe refresh ─────────────────────────────────────────
        binding.swipeRefresh.setColorSchemeResources(R.color.blue_600);
        binding.swipeRefresh.setOnRefreshListener(this::loadDashboard);

        binding.btnNotifications.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit();
        });

// Show unread badge count
        int unread = NotificationStore.getUnreadCount(requireContext());
        if (unread > 0) {
            android.util.Log.d("VeriStay", "Unread notifications: " + unread);
            // badge shown on bell icon
        }

        loadDashboard();
    }

    // =============================================
    // Logout
    // =============================================

    private void showLogoutDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of VeriStay?")
                .setPositiveButton("Sign Out", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Cancel all WorkManager jobs
        WorkManager.getInstance(requireContext()).cancelAllWork();

        // Clear saved session
        session.clearSession();

        // Reset Retrofit so next login gets fresh token
        RetrofitClient.reset();

        // Navigate to LoginActivity and clear entire back stack
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // =============================================
    // Load Dashboard
    // =============================================

    private void loadDashboard() {
        if (binding == null) return;
        binding.swipeRefresh.setRefreshing(true);

        String userId = session.getUserId();
        android.util.Log.d("VeriStay", "Loading dashboard for: " + userId);

        api.getStudentDashboard(userId)
                .enqueue(new Callback<Models.ApiResponse<Models.StudentDashboard>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            @NonNull Response<Models.ApiResponse<Models.StudentDashboard>> response) {

                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        android.util.Log.d("VeriStay",
                                "Dashboard code: " + response.code());

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {
                            bindDashboard(response.body().data);
                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "null";
                                android.util.Log.e("VeriStay",
                                        "Dashboard error: " + err);
                                Toast.makeText(getContext(),
                                        "Failed to load: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                android.util.Log.e("VeriStay",
                                        "Parse error: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        android.util.Log.e("VeriStay",
                                "Dashboard failure: " + t.getMessage());
                        Toast.makeText(getContext(),
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =============================================
    // Bind Dashboard Data
    // =============================================

    private void bindDashboard(Models.StudentDashboard data) {
        if (binding == null || data == null) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "ZA"));

        // ── Stats ─────────────────────────────────────────────────
        binding.tvApplicationsCount.setText(String.valueOf(data.applicationsCount));
        binding.tvApprovedCount.setText(String.valueOf(data.approvedCount));
        binding.tvPaymentsCount.setText(String.valueOf(data.paymentsCount));
        binding.tvMaintenanceCount.setText(String.valueOf(data.requestsCount));

        // ── Active tenancy ────────────────────────────────────────
        if (data.activeTenancy != null) {
            Models.Tenancy t = data.activeTenancy;
            binding.cardTenancy.setVisibility(View.VISIBLE);
            binding.cardBudget.setVisibility(View.GONE);

            String title = (t.propertyTitle != null
                    && !t.propertyTitle.isEmpty()
                    && !t.propertyTitle.equals("string"))
                    ? t.propertyTitle
                    : "Property #" + t.propertyId;
            binding.tvTenancyProperty.setText(title);

            binding.tvTenancyLocation.setText(
                    t.location != null ? t.location : "");

            binding.tvTenancyRent.setText(
                    "R " + fmt.format(t.monthlyRent) + " / month");

            binding.tvTenancyLandlord.setText(
                    t.landlordName != null ? t.landlordName : "—");

            if (t.leaseEndDate != null && t.leaseEndDate.length() >= 10) {
                binding.tvLeaseEnd.setText(
                        t.leaseEndDate.substring(0, 10));
            }

        } else {
            binding.cardTenancy.setVisibility(View.GONE);
            binding.cardBudget.setVisibility(View.VISIBLE);

            if (data.student != null && data.student.budget > 0) {
                binding.tvBudget.setText(
                        "R " + fmt.format(data.student.budget) + " / month");
            } else {
                binding.tvBudget.setText("R 0 / month");
            }
        }

        // ── Approval alert ────────────────────────────────────────
        if (data.approvedCount > 0) {
            binding.cardApprovalAlert.setVisibility(View.VISIBLE);
            binding.tvApprovalAlertText.setText(
                    "You have " + data.approvedCount +
                            " approved offer(s)! Check your Applications tab.");
        } else {
            binding.cardApprovalAlert.setVisibility(View.GONE);
        }

        // ── Latest announcement ───────────────────────────────────
        if (data.announcements != null && !data.announcements.isEmpty()) {
            Models.Announcement ann = data.announcements.get(0);
            binding.cardAnnouncement.setVisibility(View.VISIBLE);
            binding.tvAnnouncementMessage.setText(
                    ann.message != null ? ann.message : "");
            binding.tvAnnouncementLandlord.setText(
                    ann.landlordName != null ? ann.landlordName : "");
        } else {
            binding.cardAnnouncement.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}