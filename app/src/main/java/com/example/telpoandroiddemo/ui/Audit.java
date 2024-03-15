package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.viewmodels.AuditViewModel;

public class Audit extends AppCompatActivity {

    private AuditViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(Audit.this));

        viewModel = new ViewModelProvider(this).get(AuditViewModel.class);
        viewModel.getRecordLogs(Audit.this).observe(this, recordLogs -> {
            recyclerView.setAdapter(new AuditAdapter(Audit.this, recordLogs));
        });

        findViewById(R.id.btn_home).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btn_clear_table).setOnClickListener(v -> {
            viewModel.purgeTable(Audit.this);
        });

    }
}