package com.veristay.app;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.veristay.app.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter
        extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    public interface OnReadListener   { void onRead(Models.AppNotification n);   }
    public interface OnDeleteListener { void onDelete(Models.AppNotification n); }

    private List<Models.AppNotification> items = new ArrayList<>();
    private final OnReadListener   onRead;
    private final OnDeleteListener onDelete;

    public NotificationsAdapter(OnReadListener onRead, OnDeleteListener onDelete) {
        this.onRead   = onRead;
        this.onDelete = onDelete;
    }

    public void setNotifications(List<Models.AppNotification> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.AppNotification n = items.get(position);

        holder.tvTitle.setText(n.title);
        holder.tvMessage.setText(n.message);

        // Format date
        holder.tvTime.setText(formatDate(n.createdAt));

        // ── Type icon + stripe + tag ──────────────────────────────
        switch (n.type != null ? n.type : "general") {
            case "application":
                holder.tvIcon.setText("📋");
                holder.viewStripe.setBackgroundColor(Color.parseColor("#2563EB"));
                holder.tvTag.setText("Application");
                holder.tvTag.setTextColor(Color.parseColor("#1D4ED8"));
                holder.tvTag.setBackgroundColor(Color.parseColor("#DBEAFE"));
                break;
            case "payment":
                holder.tvIcon.setText("💳");
                holder.viewStripe.setBackgroundColor(Color.parseColor("#059669"));
                holder.tvTag.setText("Payment");
                holder.tvTag.setTextColor(Color.parseColor("#065F46"));
                holder.tvTag.setBackgroundColor(Color.parseColor("#D1FAE5"));
                break;
            case "maintenance":
                holder.tvIcon.setText("🔧");
                holder.viewStripe.setBackgroundColor(Color.parseColor("#F97316"));
                holder.tvTag.setText("Maintenance");
                holder.tvTag.setTextColor(Color.parseColor("#9A3412"));
                holder.tvTag.setBackgroundColor(Color.parseColor("#FFEDD5"));
                break;
            case "announcement":
                holder.tvIcon.setText("📢");
                holder.viewStripe.setBackgroundColor(Color.parseColor("#7C3AED"));
                holder.tvTag.setText("Announcement");
                holder.tvTag.setTextColor(Color.parseColor("#5B21B6"));
                holder.tvTag.setBackgroundColor(Color.parseColor("#EDE9FE"));
                break;
            default:
                holder.tvIcon.setText("🔔");
                holder.viewStripe.setBackgroundColor(Color.parseColor("#6B7280"));
                holder.tvTag.setText("General");
                holder.tvTag.setTextColor(Color.parseColor("#374151"));
                holder.tvTag.setBackgroundColor(Color.parseColor("#F3F4F6"));
        }

        // ── Unread indicator ──────────────────────────────────────
        if (!n.isRead) {
            holder.card.setCardBackgroundColor(Color.parseColor("#F0F4FF"));
            holder.viewUnread.setVisibility(View.VISIBLE);
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.viewUnread.setVisibility(View.GONE);
        }

        // ── Tap to mark as read ───────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            if (!n.isRead) onRead.onRead(n);
        });

        // ── Long press to delete ──────────────────────────────────
        holder.itemView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Notification")
                    .setMessage("Remove this notification?")
                    .setPositiveButton("Delete", (d, w) -> onDelete.onDelete(n))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            // Show just date portion
            return isoDate.substring(0, 10);
        } catch (Exception e) {
            return isoDate;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvIcon, tvTitle, tvMessage, tvTime, tvTag;
        View     viewStripe, viewUnread;

        ViewHolder(View v) {
            super(v);
            card       = v.findViewById(R.id.card_notification);
            tvIcon     = v.findViewById(R.id.tv_icon);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvMessage  = v.findViewById(R.id.tv_message);
            tvTime     = v.findViewById(R.id.tv_time);
            tvTag      = v.findViewById(R.id.tv_tag);
            viewStripe = v.findViewById(R.id.view_stripe);
            viewUnread = v.findViewById(R.id.view_unread);
        }
    }
}