package com.example.telpoandroiddemo.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telpoandroiddemo.R;

public class AuditViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView codeView, datetimeView;
    public AuditViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        codeView = itemView.findViewById(R.id.validation_code);
        datetimeView = itemView.findViewById(R.id.validation_datetime);
    }
}
