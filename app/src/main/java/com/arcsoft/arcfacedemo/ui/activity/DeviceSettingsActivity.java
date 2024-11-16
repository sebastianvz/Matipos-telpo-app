package com.arcsoft.arcfacedemo.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class DeviceSettingsActivity extends BaseActivity {

    SwitchMaterial switchFaceRecognition;
    SwitchMaterial switchCameraLight;
    SwitchMaterial switchQrReaderEnable;
    SwitchMaterial switchNfcReaderEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(attributes);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        initView();
    }

    private void initView() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
        });

        switchFaceRecognition = findViewById(R.id.switchFaceRecognition);
        switchFaceRecognition.setChecked(ConfigUtil.isMatiposFaceRecognitionEnable(DeviceSettingsActivity.this));

        switchCameraLight = findViewById(R.id.switchCameraLight);
        switchCameraLight.setChecked(ConfigUtil.isWhiteLightEnable(DeviceSettingsActivity.this));

        switchQrReaderEnable = findViewById(R.id.switchQrReader);
        switchQrReaderEnable.setChecked(ConfigUtil.isMatiposIsQrReaderEnable(DeviceSettingsActivity.this));

        switchNfcReaderEnable = findViewById(R.id.switchNfcReader);
        switchNfcReaderEnable.setChecked(ConfigUtil.isMatiposIsNfcReaderEnable(DeviceSettingsActivity.this));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSettingsActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog, null);
            dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(dialogView);

            // Optionally, set title and buttons
            builder.setTitle("Update parameters");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean isError = true;
                    if (ConfigUtil.setMatiposFaceRecognition(DeviceSettingsActivity.this, switchFaceRecognition.isChecked())) {
                        if (ConfigUtil.setWhiteLight(DeviceSettingsActivity.this, switchCameraLight.isChecked())) {
                            if (ConfigUtil.setQrReader(DeviceSettingsActivity.this, switchQrReaderEnable.isChecked())) {
                                if (ConfigUtil.setNfcReader(DeviceSettingsActivity.this, switchNfcReaderEnable.isChecked())) {
                                    isError = false;
                                }
                            }
                        }
                    }
                    showToast(isError ? "Error" : "Ok");
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchFaceRecognition.setChecked(ConfigUtil.isMatiposFaceRecognitionEnable(DeviceSettingsActivity.this));
                    switchNfcReaderEnable.setChecked(ConfigUtil.isMatiposIsNfcReaderEnable(DeviceSettingsActivity.this));
                    switchQrReaderEnable.setChecked(ConfigUtil.isMatiposIsQrReaderEnable(DeviceSettingsActivity.this));
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }
}