package com.arcsoft.arcfacedemo.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;
import com.arcsoft.arcfacedemo.ui.activity.HomeActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
            holder.codeView.setTextColor(items.get(position).parseResponse().getStatus() ? Color.BLACK : Color.RED);

            holder.datetimeView.setText(items.get(position).requestDatetime);

            try {
                holder.movementType.setBackgroundResource(items.get(position).getOperationType().equals("EXIT") ? R.drawable.baseline_arrow_circle_left_24 : R.drawable.baseline_arrow_circle_right_24);
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
                } else if (items.get(position).getFaceId() > 0) {
                    Glide.with(holder.imageView)
                            .load(items.get(position).getFaceId())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .error(items.get(position).parseResponse().getStatus() ? R.drawable.ok : R.drawable.no)
                            .into(holder.imageView);
                    return;
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
