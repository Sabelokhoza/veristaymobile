package com.veristay.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.veristay.app.ApiService;
import com.veristay.app.MaintenanceAdapter;
import com.veristay.app.Models;
import com.veristay.app.NotificationHelper;
import com.veristay.app.R;

import com.veristay.app.RetrofitClient;
import com.veristay.app.SessionManager;
import com.veristay.app.databinding.FragmentMaintenanceBinding;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceFragment extends Fragment {

    private FragmentMaintenanceBinding binding;
    private SessionManager session;
    private ApiService api;
    private MaintenanceAdapter adapter;
    private int propertyId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMaintenanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        adapter = new MaintenanceAdapter();
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRequests.setAdapter(adapter);


        binding.swipeRefresh.setOnRefreshListener(this::loadRequests);

        // Get property ID from dashboard first
        loadPropertyId();
    }

    private void loadPropertyId() {
        binding.swipeRefresh.setRefreshing(true);
        api.getStudentDashboard(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<Models.StudentDashboard>>() {
                    @Override
                    public void onResponse(
                            Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            Response<Models.ApiResponse<Models.StudentDashboard>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().data != null
                                && response.body().data.activeTenancy != null) {
                            propertyId = response.body().data.activeTenancy.propertyId;
                        }
                        loadRequests();
                    }

                    @Override
                    public void onFailure(
                            Call<Models.ApiResponse<Models.StudentDashboard>> call,
                            Throwable t) {
                        loadRequests();
                    }
                });
    }

    private void loadRequests() {
        binding.swipeRefresh.setRefreshing(true);
        api.getStudentMaintenanceRequests(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<List<Models.MaintenanceRequest>>>() {
                    @Override
                    public void onResponse(
                            Call<Models.ApiResponse<List<Models.MaintenanceRequest>>> call,
                            Response<Models.ApiResponse<List<Models.MaintenanceRequest>>> response) {

                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().data != null) {

                            List<Models.MaintenanceRequest> requests = response.body().data;
                            if (!requests.isEmpty()) {
                                adapter.setRequests(requests);
                                binding.tvEmpty.setVisibility(View.GONE);
                                binding.rvRequests.setVisibility(View.VISIBLE);

                                // Check for updates and notify
                                checkForUpdates(requests);
                            } else {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                                binding.rvRequests.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Models.ApiResponse<List<Models.MaintenanceRequest>>> call,
                            Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkForUpdates(List<Models.MaintenanceRequest> requests) {
        // Simulate push notification for in-progress requests
        for (Models.MaintenanceRequest req : requests) {
            if (req.status == 1) { // InProgress
                new NotificationHelper(requireContext()).showMaintenanceNotification(
                        "🔧 Maintenance Update",
                        "Your request '" + req.title + "' is now being attended to.",
                        req.id
                );
                break;
            }
        }
    }

    private void showNewRequestDialog() {
        if (propertyId == 0) {
            Toast.makeText(getContext(),
                    "You need an active tenancy to submit a maintenance request",
                    Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_new_maintenance, null);

        EditText etTitle       = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        Spinner  spinnerPriority = dialogView.findViewById(R.id.spinner_priority);

        String[] priorities = {"Low", "Medium", "High", "Emergency"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                priorities);
        spinnerPriority.setAdapter(priorityAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Maintenance Request")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String desc  = etDescription.getText().toString().trim();
                    int priority = spinnerPriority.getSelectedItemPosition();

                    if (title.isEmpty() || desc.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitRequest(title, desc, priority);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRequest(String title, String description, int priority) {

        Models.AddMaintenanceRequest body =
                new Models.AddMaintenanceRequest(
                        propertyId,
                        title,
                        description,
                        priority
                );

        api.addMaintenanceRequest(
                session.getUserId(),
                body
        ).enqueue(new Callback<Models.ApiResponse<Models.MaintenanceRequest>>() {

            @Override
            public void onResponse(
                    Call<Models.ApiResponse<Models.MaintenanceRequest>> call,
                    Response<Models.ApiResponse<Models.MaintenanceRequest>> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().success) {

                    Toast.makeText(
                            getContext(),
                            "Request submitted ✅",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadRequests();

                } else {

                    Log.e("MAINTENANCE",
                            "HTTP " + response.code());

                    try {
                        if (response.errorBody() != null) {
                            Log.e(
                                    "MAINTENANCE",
                                    response.errorBody().string()
                            );
                        }
                    } catch (Exception e) {
                        Log.e("MAINTENANCE", "Error reading response", e);
                    }

                    Toast.makeText(
                            getContext(),
                            "Failed: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Models.ApiResponse<Models.MaintenanceRequest>> call,
                    Throwable t) {

                Log.e("MAINTENANCE", "Network error", t);

                Toast.makeText(
                        getContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}