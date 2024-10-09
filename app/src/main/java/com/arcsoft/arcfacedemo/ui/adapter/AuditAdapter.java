package com.arcsoft.arcfacedemo.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;

import java.io.File;
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
                if (items.get(position).getFaceEntity() != null) {
                    String imagePath = items.get(position).getFaceEntity().getImagePath();
                    if (imagePath != null) {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            holder.imageView.setImageBitmap(myBitmap);
                            return;
                        }
                    }
                }
                holder.imageView.setBackgroundResource(items.get(position).parseResponse().getStatus() ? R.drawable.ok : R.drawable.no);
            } catch (Exception e) {
                holder.imageView.setBackgroundResource(R.drawable.warning);
            }
        } catch (Exception ignored) {
            Log.i("Error", "onBindViewHolder: ");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
