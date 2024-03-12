package com.example.telpoandroiddemo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.domain.entities.RecordLog;
import com.example.telpoandroiddemo.ui.AuditViewHolder;

import java.util.List;

public class AuditAdapter extends RecyclerView.Adapter<AuditViewHolder> {

    Context context;
    List<RecordLog> items;

    public AuditAdapter(Context context, List<RecordLog> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AuditViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_record_log_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        holder.codeView.setText(items.get(position).getRequestModel().getEntryCode());
        holder.datetimeView.setText(items.get(position).requestDatetime);

        try {
            holder.imageView.setBackgroundResource(items.get(position).getResponseModel().getStatus() ? R.drawable.ok : R.drawable.no);
        } catch (Exception e) {
            holder.imageView.setBackgroundResource(R.drawable.warning);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
