package com.veristay.app;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.veristay.app.ApiService;
import com.veristay.app.Models;
import com.veristay.app.NotificationHelper;
import com.veristay.app.RetrofitClient;
import com.veristay.app.SessionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class PaymentReminderWorker extends Worker {

    private static final String TAG = "PaymentReminderWorker";

    public PaymentReminderWorker(@NonNull Context context,
                                 @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Running payment reminder check...");

        SessionManager session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn() || !session.isStudent()) {
            return Result.success();
        }

        try {
            ApiService api = RetrofitClient
                    .getInstance(session)
                    .getApiService();

            Call<Models.ApiResponse<Models.StudentPaymentSummary>> call =
                    api.getStudentPaymentSummary(session.getUserId());

            Response<Models.ApiResponse<Models.StudentPaymentSummary>> response =
                    call.execute();

            if (response.isSuccessful()
                    && response.body() != null
                    && response.body().success
                    && response.body().data != null) {

                Models.StudentPaymentSummary summary = response.body().data;
                checkAndNotify(summary, session);
            }

        } catch (Exception e) {
            Log.e(TAG, "Payment check failed: " + e.getMessage());
        }

        return Result.success();
    }

    private void checkAndNotify(Models.StudentPaymentSummary summary,
                                SessionManager session) {
        NotificationHelper helper =
                new NotificationHelper(getApplicationContext());

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "ZA"));

        if (summary.payments == null) return;

        // ── Overdue payments ──────────────────────────────────────────
        long overdueCount = summary.payments.stream()
                .filter(p -> p.status == 2)
                .count();

        if (overdueCount > 0) {
            double totalOverdue = summary.payments.stream()
                    .filter(p -> p.status == 2)
                    .mapToDouble(p -> p.amount)
                    .sum();

            helper.showPaymentNotification(
                    "⚠️ Overdue Rent Payment",
                    String.format("You have %d overdue payment(s) totalling R %s. " +
                                    "Please pay as soon as possible.",
                            overdueCount, fmt.format(totalOverdue)),
                    1001
            );
        }

        // ── Upcoming payment (due within 7 days) ──────────────────────
        List<Models.RentPayment> pending = summary.payments.stream()
                .filter(p -> p.status == 0)
                .collect(java.util.stream.Collectors.toList());

        if (!pending.isEmpty()) {
            Models.RentPayment next = pending.get(0);
            helper.showPaymentNotification(
                    "💳 Rent Payment Due Soon",
                    String.format("Your rent of R %s is due on %s.",
                            fmt.format(next.amount),
                            formatDate(next.dueDate)),
                    1002
            );
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            // Simple slice — ISO 8601 to "dd MMM yyyy"
            return isoDate.substring(0, 10);
        } catch (Exception e) {
            return isoDate;
        }
    }
}