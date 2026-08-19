package com.veristay.app;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.veristay.app.R;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceAdapter
        extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private List<Models.MaintenanceRequest> items = new ArrayList<>();

    public void setRequests(List<Models.MaintenanceRequest> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.MaintenanceRequest req = items.get(position);

        holder.tvTitle.setText(req.title);
        holder.tvDescription.setText(req.description);

        String[] priorities       = {"Low",     "Medium",  "High",    "Emergency"};
        String[] priorityColors   = {"#059669", "#D97706", "#DC2626", "#7C2D12"  };
        String[] priorityBgColors = {"#D1FAE5", "#FEF3C7", "#FEE2E2", "#FEE2E2"  };
        String[] priorityIcons    = {"🔵",      "🟡",      "🔴",      "🚨"       };

        int p = Math.min(req.priority, 3);

        holder.tvPriority.setText(priorityIcons[p] + " " + priorities[p] + " Priority");
        holder.tvPriority.setTextColor(Color.parseColor(priorityColors[p]));

        if (holder.tvPriorityIcon != null)
            holder.tvPriorityIcon.setText(priorityIcons[p]);

        if (holder.viewPriorityBg != null)
            holder.viewPriorityBg.setBackgroundColor(
                    Color.parseColor(priorityBgColors[p]));

        // ── Status ────────────────────────────────────────────────
        switch (req.status) {
            case 0: // Open
                holder.tvStatus.setText("Open");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#DBEAFE"));
                holder.tvStatus.setTextColor(Color.parseColor("#1D4ED8"));
                if (holder.viewStatusStripe != null)
                    holder.viewStatusStripe.setBackgroundColor(
                            Color.parseColor("#2563EB"));
                break;

            case 1: // In Progress
                holder.tvStatus.setText("In Progress");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FED7AA"));
                holder.tvStatus.setTextColor(Color.parseColor("#C2410C"));
                if (holder.viewStatusStripe != null)
                    holder.viewStatusStripe.setBackgroundColor(
                            Color.parseColor("#F97316"));
                break;

            case 2: // Resolved
                holder.tvStatus.setText("Resolved ✅");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#D1FAE5"));
                holder.tvStatus.setTextColor(Color.parseColor("#065F46"));
                if (holder.viewStatusStripe != null)
                    holder.viewStatusStripe.setBackgroundColor(
                            Color.parseColor("#059669"));
                break;

            default:
                holder.tvStatus.setText("Unknown");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F3F4F6"));
                holder.tvStatus.setTextColor(Color.parseColor("#6B7280"));
                if (holder.viewStatusStripe != null)
                    holder.viewStatusStripe.setBackgroundColor(
                            Color.parseColor("#E5E7EB"));
        }

        if (req.submittedAt != null && req.submittedAt.length() >= 10) {
            holder.tvDate.setText(req.submittedAt.substring(0, 10));
        } else {
            holder.tvDate.setText("—");
        }

        if (req.landlordResponse != null
                && !req.landlordResponse.isEmpty()
                && !req.landlordResponse.equals("string")) {

            if (holder.layoutLandlordResponse != null) {
                holder.layoutLandlordResponse.setVisibility(View.VISIBLE);
                holder.tvLandlordResponse.setText(req.landlordResponse);
            } else {
                // Fallback if using old layout
                holder.tvLandlordResponse.setVisibility(View.VISIBLE);
                holder.tvLandlordResponse.setText("💬 " + req.landlordResponse);
            }
        } else {
            if (holder.layoutLandlordResponse != null) {
                holder.layoutLandlordResponse.setVisibility(View.GONE);
            } else {
                holder.tvLandlordResponse.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() { return items.size(); }



    static class ViewHolder extends RecyclerView.ViewHolder {
        // Original views
        TextView tvTitle, tvDescription, tvPriority,
                tvStatus, tvDate, tvLandlordResponse;

        // New views from modernised layout
        TextView     tvPriorityIcon;
        View         viewStatusStripe, viewPriorityBg;
        LinearLayout layoutLandlordResponse;

        ViewHolder(View v) {
            super(v);
            // Original — always present
            tvTitle            = v.findViewById(R.id.tv_title);
            tvDescription      = v.findViewById(R.id.tv_description);
            tvPriority         = v.findViewById(R.id.tv_priority);
            tvStatus           = v.findViewById(R.id.tv_status);
            tvDate             = v.findViewById(R.id.tv_date);
            tvLandlordResponse = v.findViewById(R.id.tv_landlord_response);

            // New — from modernised layout (null-safe in onBind)
            tvPriorityIcon         = v.findViewById(R.id.tv_priority_icon);
            viewStatusStripe       = v.findViewById(R.id.view_status_stripe);
            viewPriorityBg         = v.findViewById(R.id.view_priority_bg);
            layoutLandlordResponse = v.findViewById(R.id.layout_landlord_response);
        }
    }
}