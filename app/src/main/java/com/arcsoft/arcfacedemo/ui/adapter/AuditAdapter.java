package com.arcsoft.arcfacedemo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;

import java.util.List;

public class AuditAdapter extends RecyclerView.Adapter<AuditViewHolder> {

    Context context;
    List<MovementEntity> items;

    public AuditAdapter(Context context, List<MovementEntity> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AuditViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_record_log_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        try {
            holder.codeView.setText(items.get(position).parseRequest().getEntryCode());
            holder.datetimeView.setText(items.get(position).requestDatetime);

            try {
                holder.imageView.setBackgroundResource(items.get(position).parseResponse().getStatus() ? R.drawable.ok : R.drawable.no);
            } catch (Exception e) {
                holder.imageView.setBackgroundResource(R.drawable.warning);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
