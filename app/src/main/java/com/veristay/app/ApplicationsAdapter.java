package com.veristay.app;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.veristay.app.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplicationsAdapter
        extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Models.Application app); }

    private List<Models.Application> items = new ArrayList<>();
    private final OnClickListener listener;
    private final NumberFormat fmt =
            NumberFormat.getNumberInstance(new Locale("en", "ZA"));

    public ApplicationsAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setApplications(List<Models.Application> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Application app = items.get(position);

        holder.tvTitle.setText(
                app.propertyTitle != null && !app.propertyTitle.equals("string")
                        ? app.propertyTitle
                        : "Property #" + app.propertyId);

        if (app.propertyLocation != null
                && !app.propertyLocation.isEmpty()
                && !app.propertyLocation.equals("string")) {
            holder.tvLocation.setText(app.propertyLocation);
        } else {
            holder.tvLocation.setText("Location not specified");
        }

        if (app.price > 0) {
            holder.tvPrice.setText("R " + fmt.format(app.price) + " / month");
            holder.tvPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }

        if (app.appliedAt != null && app.appliedAt.length() >= 10) {
            holder.tvDate.setText("Applied: " + app.appliedAt.substring(0, 10));
        } else {
            holder.tvDate.setText("—");
        }

        switch (app.status) {

            case 0: // Pending
                holder.tvStatus.setText("Pending");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
                holder.tvStatus.setTextColor(Color.parseColor("#856404"));
                setStatusStyle(holder, "⏳", "#D97706", "#FEF3C7");
                break;

            case 1: // Approved
                holder.tvStatus.setText("✅ Approved");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#D1FAE5"));
                holder.tvStatus.setTextColor(Color.parseColor("#065F46"));
                setStatusStyle(holder, "🎉", "#059669", "#D1FAE5");
                break;

            case 2: // Rejected
                holder.tvStatus.setText("❌ Rejected");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
                holder.tvStatus.setTextColor(Color.parseColor("#991B1B"));
                setStatusStyle(holder, "❌", "#DC2626", "#FEE2E2");
                break;

            case 4: // Withdrawn
                holder.tvStatus.setText("✅ Accepted");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F3F4F6"));
                holder.tvStatus.setTextColor(Color.parseColor("#374151"));
                setStatusStyle(holder, "📋", "#9CA3AF", "#F3F4F6");
                break;

            default:
                holder.tvStatus.setText("Unknown");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F3F4F6"));
                holder.tvStatus.setTextColor(Color.parseColor("#6B7280"));
                setStatusStyle(holder, "❓", "#E5E7EB", "#F3F4F6");
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(app));
    }

    private void setStatusStyle(ViewHolder holder,
                                String icon,
                                String stripeColor,
                                String bgColor) {
        if (holder.tvStatusIcon     != null)
            holder.tvStatusIcon.setText(icon);
        if (holder.viewStatusStripe != null)
            holder.viewStatusStripe.setBackgroundColor(
                    Color.parseColor(stripeColor));
        if (holder.viewStatusBg     != null)
            holder.viewStatusBg.setBackgroundColor(
                    Color.parseColor(bgColor));
    }

    @Override
    public int getItemCount() { return items.size(); }


    static class ViewHolder extends RecyclerView.ViewHolder {
        // Original views
        TextView tvTitle, tvLocation, tvPrice, tvDate, tvStatus;

        // New views from modernised layout
        TextView tvStatusIcon;
        View     viewStatusStripe, viewStatusBg;

        ViewHolder(View v) {
            super(v);
            // Original — always present
            tvTitle    = v.findViewById(R.id.tv_title);
            tvLocation = v.findViewById(R.id.tv_location);
            tvPrice    = v.findViewById(R.id.tv_price);
            tvDate     = v.findViewById(R.id.tv_date);
            tvStatus   = v.findViewById(R.id.tv_status);

            // New — null-safe in onBind via setStatusStyle()
            tvStatusIcon     = v.findViewById(R.id.tv_status_icon);
            viewStatusStripe = v.findViewById(R.id.view_status_stripe);
            viewStatusBg     = v.findViewById(R.id.view_status_bg);
        }
    }
}