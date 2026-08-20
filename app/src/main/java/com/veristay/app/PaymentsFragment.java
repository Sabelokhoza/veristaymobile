package com.veristay.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.veristay.app.databinding.FragmentPaymentsBinding;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentsFragment extends Fragment {

    private FragmentPaymentsBinding binding;
    private SessionManager          session;
    private ApiService              api;
    private PaymentsAdapter         adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();

        // ✅ Pass both listeners to adapter
        adapter = new PaymentsAdapter(
                this::handlePayNow,
                this::handleReceipt     // 👈 calls backend first
        );

        binding.rvPayments.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.rvPayments.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadPayments);
        loadPayments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPayments();
    }

    // =============================================
    // Load payments
    // =============================================

    private void loadPayments() {
        if (binding == null) return;
        binding.swipeRefresh.setRefreshing(true);

        api.getStudentPaymentSummary(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<Models.StudentPaymentSummary>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Models.StudentPaymentSummary>> call,
                            @NonNull Response<Models.ApiResponse<Models.StudentPaymentSummary>> response) {

                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {
                            bindSummary(response.body().data);
                        } else {
                            showEmpty(true);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Models.StudentPaymentSummary>> call,
                            @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(),
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showEmpty(true);
                    }
                });
    }

    // =============================================
    // Bind summary data
    // =============================================

    private void bindSummary(Models.StudentPaymentSummary summary) {
        if (binding == null) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "ZA"));

        // Header
        binding.tvPropertyTitle.setText(
                summary.propertyTitle != null
                        && !summary.propertyTitle.equals("string")
                        ? summary.propertyTitle
                        : "Your Property");

        binding.tvMonthlyRent.setText(
                "R " + fmt.format(summary.monthlyRent) + " / month");

        // Stats
        binding.tvPaidCount.setText(String.valueOf(summary.paidCount));
        binding.tvPendingCount.setText(String.valueOf(summary.pendingCount));
        binding.tvOverdueCount.setText(String.valueOf(summary.overdueCount));

        // Totals
        binding.tvTotalPaid.setText("R " + fmt.format(summary.totalPaid));
        binding.tvTotalOwed.setText("R " + fmt.format(summary.totalOwed));

        // Overdue banner
        if (summary.overdueCount > 0) {
            binding.cardOverdueBanner.setVisibility(View.VISIBLE);
            binding.tvOverdueBannerText.setText(
                    "You have " + summary.overdueCount +
                            " overdue payment(s) totalling R " +
                            fmt.format(summary.totalOwed) +
                            ". Please pay as soon as possible.");
        } else {
            binding.cardOverdueBanner.setVisibility(View.GONE);
        }

        // Payments list
        if (summary.payments != null && !summary.payments.isEmpty()) {
            showEmpty(false);
            adapter.setPayments(summary.payments);
        } else {
            showEmpty(true);
        }
    }

    // =============================================
    // Pay Now
    // =============================================

    private void handlePayNow(Models.RentPayment payment) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage(
                        "Mark payment of R " +
                                NumberFormat.getNumberInstance(new Locale("en", "ZA"))
                                        .format(payment.amount) +
                                " as paid?")
                .setPositiveButton("Mark as Paid", (dialog, which) -> markAsPaid(payment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markAsPaid(Models.RentPayment payment) {
        // Create request body
        class MarkPaidBody {
            public int rentPaymentId = payment.id;
        }

        api.markRentPaid(new MarkPaidBody().rentPaymentId)
                .enqueue(new Callback<Models.ApiResponse<Models.RentPayment>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<Models.RentPayment>> call,
                            @NonNull Response<Models.ApiResponse<Models.RentPayment>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Payment marked as paid ✅",
                                    Toast.LENGTH_SHORT).show();
                            loadPayments(); // refresh
                        } else {
                            Toast.makeText(getContext(),
                                    "Failed to mark payment",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<Models.RentPayment>> call,
                            @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =============================================
    // ✅ Receipt — fetch signed URL then open browser
    // =============================================

    private void handleReceipt(Models.RentPayment payment) {
        // Show loading toast
        Toast.makeText(getContext(), "Loading receipt...", Toast.LENGTH_SHORT).show();

        api.getReceipt(payment.id)
                .enqueue(new Callback<Models.ApiResponse<String>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Models.ApiResponse<String>> call,
                            @NonNull Response<Models.ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {

                            String signedUrl = response.body().data;

                            // ✅ Open signed URL in browser/PDF viewer
                            try {
                                Intent intent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(signedUrl));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(getContext(),
                                        "Could not open receipt: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // ✅ Fallback — try opening stored receipt URL directly
                            if (payment.receiptUrl != null
                                    && !payment.receiptUrl.isEmpty()
                                    && payment.receiptUrl.startsWith("http")) {
                                try {
                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(payment.receiptUrl));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(getContext(),
                                            "No receipt available",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(),
                                        "Receipt not available yet",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Models.ApiResponse<String>> call,
                            @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =============================================
    // UI helpers
    // =============================================

    private void showEmpty(boolean empty) {
        if (binding == null) return;
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvPayments.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}