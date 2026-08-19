package com.veristay.app;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.veristay.app.Models;
import com.veristay.app.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder> {

    public interface PayNowListener  { void onPayNow(Models.RentPayment p); }
    public interface ReceiptListener { void onReceipt(Models.RentPayment p); }

    private List<Models.RentPayment> items = new ArrayList<>();
    private final PayNowListener  payNowListener;
    private final ReceiptListener receiptListener;
    private final NumberFormat fmt =
            NumberFormat.getNumberInstance(new Locale("en", "ZA"));

    public PaymentsAdapter(PayNowListener pay, ReceiptListener receipt) {
        this.payNowListener  = pay;
        this.receiptListener = receipt;
    }

    public void setPayments(List<Models.RentPayment> list) {
        // Sort: overdue → pending → paid
        list.sort((a, b) -> {
            int[] order = {2, 0, 1};
            return Integer.compare(indexOf(order, a.status), indexOf(order, b.status));
        });
        items = list;
        notifyDataSetChanged();
    }

    private int indexOf(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == val) return i;
        return arr.length;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.RentPayment payment = items.get(position);

        holder.tvAmount.setText("R " + fmt.format(payment.amount));

        String dueDate = payment.dueDate != null && payment.dueDate.length() >= 10
                ? payment.dueDate.substring(0, 10) : "—";
        holder.tvDueDate.setText("Due: " + dueDate);

        switch (payment.status) {

            case 0: // ── Pending ──────────────────────────────────
                holder.tvStatus.setText("⏳ Pending");
                holder.tvStatus.setTextColor(Color.parseColor("#D97706"));
                holder.tvStatusIcon.setText("💳");
                holder.viewStatusStripe.setBackgroundColor(
                        Color.parseColor("#D97706"));
                holder.viewIconBg.setBackgroundColor(
                        Color.parseColor("#FEF3C7"));
                holder.btnPayNow.setVisibility(View.VISIBLE);
                holder.btnReceipt.setVisibility(View.GONE);
                break;

            case 1: // ── Paid ─────────────────────────────────────
                String paidDate = payment.paidAt != null
                        && payment.paidAt.length() >= 10
                        ? payment.paidAt.substring(0, 10) : "";
                holder.tvStatus.setText(
                        "✅ Paid" + (paidDate.isEmpty() ? "" : "  " + paidDate));
                holder.tvStatus.setTextColor(Color.parseColor("#059669"));
                holder.tvStatusIcon.setText("✅");
                holder.viewStatusStripe.setBackgroundColor(
                        Color.parseColor("#059669"));
                holder.viewIconBg.setBackgroundColor(
                        Color.parseColor("#D1FAE5"));
                holder.btnPayNow.setVisibility(View.GONE);
                holder.btnReceipt.setVisibility(View.VISIBLE);
                break;

            case 2: // ── Overdue ───────────────────────────────────
                holder.tvStatus.setText("🚨 Overdue");
                holder.tvStatus.setTextColor(Color.parseColor("#DC2626"));
                holder.tvStatusIcon.setText("⚠️");
                holder.viewStatusStripe.setBackgroundColor(
                        Color.parseColor("#DC2626"));
                holder.viewIconBg.setBackgroundColor(
                        Color.parseColor("#FEE2E2"));
                holder.btnPayNow.setVisibility(View.VISIBLE);
                holder.btnReceipt.setVisibility(View.GONE);
                break;

            default:
                holder.tvStatus.setText("Unknown");
                holder.tvStatusIcon.setText("❓");
                holder.viewStatusStripe.setBackgroundColor(
                        Color.parseColor("#E5E7EB"));
                holder.viewIconBg.setBackgroundColor(
                        Color.parseColor("#F3F4F6"));
                holder.btnPayNow.setVisibility(View.GONE);
                holder.btnReceipt.setVisibility(View.GONE);
        }

        holder.btnPayNow.setOnClickListener(v -> payNowListener.onPayNow(payment));
        holder.btnReceipt.setOnClickListener(v -> receiptListener.onReceipt(payment));
    }

    @Override
    public int getItemCount() { return items.size(); }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvAmount, tvDueDate, tvStatus, tvStatusIcon;
        MaterialButton btnPayNow, btnReceipt;
        View           viewStatusStripe, viewIconBg;

        ViewHolder(View v) {
            super(v);
            tvAmount         = v.findViewById(R.id.tv_amount);
            tvDueDate        = v.findViewById(R.id.tv_due_date);
            tvStatus         = v.findViewById(R.id.tv_status);
            tvStatusIcon     = v.findViewById(R.id.tv_status_icon);
            btnPayNow        = v.findViewById(R.id.btn_pay_now);
            btnReceipt       = v.findViewById(R.id.btn_receipt);
            viewStatusStripe = v.findViewById(R.id.view_status_stripe);
            viewIconBg       = v.findViewById(R.id.view_icon_bg);
        }
    }
}