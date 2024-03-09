package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.GnssAntennaInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.viewmodels.MainViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.internal.operators.parallel.ParallelRunOn;


public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private TextView textViewQrCode;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hideSystemUI();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
            viewModel.startApplication(getApplication());
        }

        textViewQrCode = findViewById(R.id.qr_code);

        viewModel.getAllConfigurations().observe(this, configurations -> {
            if (configurations.isEmpty()) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        viewModel.getCodeData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.length() > 1) {
                    textViewQrCode.setText(s);
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Search");
                    progressDialog.setMessage("Validating code " + s);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    SendCommand sendCommand = new SendCommand();
                    sendCommand.execute(s);
                }
            }
        });

        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            if (packageInfo != null) {
                TextView versionApp = findViewById(R.id.versionApp);
                versionApp.setText(packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.telpoDevices.startDecodeReader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.telpoDevices.stopDecodeReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.telpoDevices.stopDecodeReader();
    }

    // TODO: Create a new class
    private class SendCommand {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void execute (String code) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    // Put Call service here
                    SystemClock.sleep(5000);

                    // Put code OnPost here
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            textViewQrCode.setText("");
                        }
                    });
                }
            });
        }
    }
}