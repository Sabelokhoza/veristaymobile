package com.veristay.app;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.veristay.app.Models;
import com.veristay.app.R;


import java.util.ArrayList;
import java.util.List;

public class AnnouncementsAdapter
        extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Models.Announcement ann); }

    private List<Models.Announcement> items = new ArrayList<>();
    private final OnClickListener listener;

    public AnnouncementsAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setAnnouncements(List<Models.Announcement> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Announcement ann = items.get(position);

        holder.tvMessage.setText(ann.message);
        holder.tvLandlord.setText("📢 " + (ann.landlordName != null ? ann.landlordName : "Landlord"));

        if (ann.postedAt != null && ann.postedAt.length() >= 10) {
            holder.tvDate.setText(ann.postedAt.substring(0, 10));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(ann));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvLandlord, tvDate;
        ViewHolder(View v) {
            super(v);
            tvMessage  = v.findViewById(R.id.tv_message);
            tvLandlord = v.findViewById(R.id.tv_landlord);
            tvDate     = v.findViewById(R.id.tv_date);
        }
    }
}