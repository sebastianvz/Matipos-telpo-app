package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.common.apiutil.ResultCode;
import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.util.SDKUtil;
import com.common.callback.IDecodeReaderListener;
import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.viewmodels.MainViewModel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private DecodeReader decodeReader;
    private Boolean statusQrCode;
    private TextView textViewQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SDKUtil.getInstance(this).initSDK();
        statusQrCode = false;

        hideSystemUI();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewQrCode = findViewById(R.id.qr_code);

        if (viewModel == null)
            viewModel = new MainViewModel(getApplication());

        viewModel.getAllConfigurations().observe(this, configurations -> {
            Log.d("APP-LP", "onCreate: " + configurations);
            if (configurations.isEmpty()) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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

        findViewById(R.id.main_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initQrReader();
            }
        });

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

    private void initQrReader() {
        boolean statusQrCode = decodeReader.open(115200) == ResultCode.SUCCESS;
        if (statusQrCode) {
            decodeReader.setDecodeReaderListener(new IDecodeReaderListener() {
                @Override
                public void onRecvData(byte[] bytes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bytes.length != 0) {
                                String str = new String(bytes, StandardCharsets.UTF_8);
                                textViewQrCode.setText(str);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (decodeReader == null) {
            decodeReader = new DecodeReader(this);//初始化
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (decodeReader != null) {
            decodeReader.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (decodeReader != null) {
            decodeReader.close();
        }
    }
}