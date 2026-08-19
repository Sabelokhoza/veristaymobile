package com.veristay.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.veristay.app.ApiService;
import com.veristay.app.Models;
import com.veristay.app.NotificationHelper;
import com.veristay.app.RetrofitClient;
import com.veristay.app.SessionManager;
import com.veristay.app.databinding.FragmentPaymentsBinding;


import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentsFragment extends Fragment {

    private FragmentPaymentsBinding binding;
    private SessionManager session;
    private ApiService api;
    private PaymentsAdapter adapter;
    private NumberFormat fmt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        api     = RetrofitClient.getInstance(session).getApiService();
        fmt     = NumberFormat.getNumberInstance(new Locale("en", "ZA"));

        adapter = new PaymentsAdapter(this::onPayNow, this::onViewReceipt);
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPayments.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadPayments);
        loadPayments();
    }

    private void loadPayments() {
        binding.swipeRefresh.setRefreshing(true);

        api.getStudentPaymentSummary(session.getUserId())
                .enqueue(new Callback<Models.ApiResponse<Models.StudentPaymentSummary>>() {
                    @Override
                    public void onResponse(
                            Call<Models.ApiResponse<Models.StudentPaymentSummary>> call,
                            Response<Models.ApiResponse<Models.StudentPaymentSummary>> response) {

                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {
                            bindSummary(response.body().data);
                        } else {
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Models.ApiResponse<Models.StudentPaymentSummary>> call,
                            Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindSummary(Models.StudentPaymentSummary summary) {
        binding.tvPropertyTitle.setText(
                summary.propertyTitle != null && !summary.propertyTitle.equals("string")
                        ? summary.propertyTitle
                        : "Tenancy #" + summary.tenancyId);
        binding.tvMonthlyRent.setText("R " + fmt.format(summary.monthlyRent) + " / month");

        binding.tvTotalPaid.setText("R " + fmt.format(summary.totalPaid));
        binding.tvTotalOwed.setText("R " + fmt.format(summary.totalOwed));
        binding.tvPaidCount.setText(String.valueOf(summary.paidCount));
        binding.tvPendingCount.setText(String.valueOf(summary.pendingCount));
        binding.tvOverdueCount.setText(String.valueOf(summary.overdueCount));

        // Overdue banner
        if (summary.overdueCount > 0) {
            binding.cardOverdueBanner.setVisibility(View.VISIBLE);
            binding.tvOverdueBannerText.setText(
                    summary.overdueCount + " overdue payment(s) — R " +
                            fmt.format(summary.totalOwed) + " outstanding");
        } else {
            binding.cardOverdueBanner.setVisibility(View.GONE);
        }

        if (summary.payments != null && !summary.payments.isEmpty()) {
            adapter.setPayments(summary.payments);
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvPayments.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvPayments.setVisibility(View.GONE);
        }
    }

    private void onPayNow(Models.RentPayment payment) {
        // Show simulated card payment dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage("Pay R " + fmt.format(payment.amount) +
                        " for " + formatDate(payment.dueDate) + "?\n\n" +
                        "This is a simulated payment for demonstration purposes only.")
                .setPositiveButton("Confirm Payment", (dialog, which) ->
                        processPayment(payment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment(Models.RentPayment payment) {
        binding.swipeRefresh.setRefreshing(true);

        api.markRentPaid(new ApiService.MarkRentPaidDto(payment.id))
                .enqueue(new Callback<Models.ApiResponse<Models.RentPayment>>() {
                    @Override
                    public void onResponse(
                            Call<Models.ApiResponse<Models.RentPayment>> call,
                            Response<Models.ApiResponse<Models.RentPayment>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success) {

                            // Show payment confirmation notification
                            NumberFormat fmt2 = NumberFormat.getNumberInstance(new Locale("en", "ZA"));
                            new NotificationHelper(requireContext()).showPaymentNotification(
                                    "✅ Payment Confirmed",
                                    "Your rent payment of R " + fmt2.format(payment.amount) +
                                            " has been recorded successfully.",
                                    (int) (System.currentTimeMillis() / 1000)
                            );

                            Toast.makeText(getContext(),
                                    "Payment confirmed! ✅", Toast.LENGTH_LONG).show();
                            loadPayments(); // refresh
                        } else {
                            binding.swipeRefresh.setRefreshing(false);
                            Toast.makeText(getContext(),
                                    "Payment failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Models.ApiResponse<Models.RentPayment>> call,
                            Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(),
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onViewReceipt(Models.RentPayment payment) {
        if (payment.receiptUrl == null || payment.receiptUrl.isEmpty()
                || payment.receiptUrl.equals("string")) {
            Toast.makeText(getContext(),
                    "Receipt reference: VSTAY-" + payment.tenancyId + "-" + payment.id,
                    Toast.LENGTH_LONG).show();
        } else {
            // Open receipt URL in browser
            android.content.Intent intent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(payment.receiptUrl));
            startActivity(intent);
        }
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "—";
        return iso.substring(0, 10);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}