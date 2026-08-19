package com.veristay.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.veristay.app.databinding.FragmentAnnouncementsBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsFragment extends Fragment {

    private FragmentAnnouncementsBinding binding;
    private SessionManager session;
    private ApiService api;
    private AnnouncementsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        adapter = new AnnouncementsAdapter(this::showDetail);
        binding.rvAnnouncements.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.rvAnnouncements.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadAnnouncements);
        loadAnnouncements();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnnouncements();
    }

    // =============================================
    // Load from dedicated endpoint
    // =============================================

    private void loadAnnouncements() {
        if (binding == null) return;
        binding.swipeRefresh.setRefreshing(true);

        String studentId = session.getUserId();
        android.util.Log.d("VeriStay",
                "Loading announcements for student: " + studentId);

        api.getStudentAnnouncements(studentId)
                .enqueue(new Callback<Models.ApiResponse<List<Models.Announcement>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<List<Models.Announcement>>> call,
                            @NonNull Response<Models.ApiResponse<List<Models.Announcement>>> response) {

                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        android.util.Log.d("VeriStay",
                                "Announcements response: " + response.code());

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {

                            List<Models.Announcement> announcements =
                                    response.body().data;

                            android.util.Log.d("VeriStay",
                                    "Announcements count: " + announcements.size());

                            if (!announcements.isEmpty()) {
                                adapter.setAnnouncements(announcements);
                                binding.tvEmpty.setVisibility(View.GONE);
                                binding.rvAnnouncements.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                                binding.rvAnnouncements.setVisibility(View.GONE);
                            }

                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "null";
                                android.util.Log.e("VeriStay",
                                        "Announcements error: " + err);
                            } catch (Exception e) {
                                android.util.Log.e("VeriStay", e.getMessage());
                            }
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                            binding.rvAnnouncements.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<List<Models.Announcement>>> call,
                            @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        android.util.Log.e("VeriStay",
                                "Announcements network error: " + t.getMessage());
                        Toast.makeText(getContext(),
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.rvAnnouncements.setVisibility(View.GONE);
                    }
                });
    }

    // =============================================
    // Show full announcement detail
    // =============================================

    private void showDetail(Models.Announcement ann) {
        String date = (ann.postedAt != null && ann.postedAt.length() >= 10)
                ? ann.postedAt.substring(0, 10) : "";

        new AlertDialog.Builder(requireContext())
                .setTitle("📢 " + (ann.landlordName != null
                        ? ann.landlordName : "Landlord"))
                .setMessage(ann.message
                        + (date.isEmpty() ? "" : "\n\nPosted: " + date))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}