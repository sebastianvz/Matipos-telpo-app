package com.arcsoft.arcfacedemo.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.adapter.AuditAdapter;
import com.arcsoft.arcfacedemo.ui.viewmodel.AuditViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;

public class AuditActivity extends BaseActivity {

    private AuditViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(attributes);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        initViewModel();

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btn_load_more).setOnClickListener(v -> {
            viewModel.loadMoreMovements();
        });

        findViewById(R.id.btn_last).setOnClickListener(v -> {
            viewModel.lastPage();
        });

        findViewById(R.id.btn_clear).setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(AuditActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog, null);
            dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(dialogView);

            // Optionally, set title and buttons
            builder.setTitle("Seguro de borrar registros");
            builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    viewModel.purge();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }

    private void initViewModel() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(AuditActivity.this));

        viewModel = new ViewModelProvider(this).get(AuditViewModel.class);
        viewModel.init();
        viewModel.movements().observe(this, movementEntities -> {
            recyclerView.setAdapter(new AuditAdapter(AuditActivity.this, movementEntities));
        });


    }
}