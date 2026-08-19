package com.veristay.app;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.veristay.app.ApiService;
import com.veristay.app.ApplicationsAdapter;
import com.veristay.app.Models;
import com.veristay.app.NotificationHelper;
import com.veristay.app.RetrofitClient;
import com.veristay.app.SessionManager;
import com.veristay.app.databinding.FragmentApplicationsBinding;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplicationsFragment extends Fragment {

    private FragmentApplicationsBinding binding;
    private SessionManager session;
    private ApiService api;
    private ApplicationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentApplicationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        adapter = new ApplicationsAdapter(app -> showStatusDetail(app));
        binding.rvApplications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvApplications.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadApplications);
        loadApplications();
    }

    private void loadApplications() {
        binding.swipeRefresh.setRefreshing(true);

        api.getStudentDashboard(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<Models.StudentDashboard>>() {
                    @Override
                    public void onResponse(
                            Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            Response<Models.ApiResponse<Models.StudentDashboard>> response) {

                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {

                            Models.StudentDashboard data = response.body().data;

                            if (data.applications != null && !data.applications.isEmpty()) {
                                adapter.setApplications(data.applications);
                                binding.tvEmpty.setVisibility(View.GONE);
                                binding.rvApplications.setVisibility(View.VISIBLE);

                                // Simulate push notification for approved applications
                                checkForApproved(data);
                            } else {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                                binding.rvApplications.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkForApproved(Models.StudentDashboard data) {
        if (data.applications == null) return;

        long approvedCount = data.applications.stream()
                .filter(a -> a.status == 1).count();

        if (approvedCount > 0) {
            // Simulate "approved" push notification
            new NotificationHelper(requireContext()).showApplicationNotification(
                    "🎉 Application Approved!",
                    "Your application has been approved. Check your dashboard.",
                    (int) (System.currentTimeMillis() / 1000)
            );
        }

        // Waiting list update simulation
        if (data.waitingList != null && !data.waitingList.isEmpty()) {
            new NotificationHelper(requireContext()).showApplicationNotification(
                    "📋 Waiting List Update",
                    "A spot may be available for a property on your waiting list.",
                    (int) (System.currentTimeMillis() / 1000) + 1
            );
        }
    }

    private void showStatusDetail(Models.Application app) {
        String statusText;
        switch (app.status) {
            case 0: statusText = "Pending review by landlord"; break;
            case 1: statusText = "✅ Approved! Contact your landlord."; break;
            case 2: statusText = "❌ Rejected. You can apply to other properties."; break;
            case 4: statusText = "Withdrawn"; break;
            default: statusText = "Unknown status";
        }
        Toast.makeText(getContext(), statusText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}