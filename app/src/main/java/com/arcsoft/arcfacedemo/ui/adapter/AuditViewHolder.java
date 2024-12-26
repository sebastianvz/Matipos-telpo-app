package com.arcsoft.arcfacedemo.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;

public class AuditViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView, movementType;
    TextView codeView, datetimeView;
    public AuditViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        codeView = itemView.findViewById(R.id.validation_code);
        datetimeView = itemView.findViewById(R.id.validation_datetime);
        movementType = itemView.findViewById(R.id.image_view_movement_type);
    }
}
