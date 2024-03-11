package com.example.telpoandroiddemo.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.viewmodels.SettingsViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel viewModel;
    private TextInputEditText textInputEditTextUrlBase;
    private TextInputEditText textInputEditTextUrlImage;
    private TextInputEditText textInputEditTextSecondsInGreen;
    private TextInputEditText textInputEditTextSecondsInRed;
    private TextInputEditText textInputEditTextDeviceId;
    private SwitchMaterial switchMaterialNFC;
    private SwitchMaterial switchMaterialQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hideSystemUI();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        viewModel = new SettingsViewModel(getApplication());
        textInputEditTextUrlBase = findViewById(R.id.url_base);
        textInputEditTextUrlImage = findViewById(R.id.url_image);
        textInputEditTextSecondsInGreen = findViewById(R.id.seconds_in_green);
        textInputEditTextSecondsInRed = findViewById(R.id.seconds_in_red);
        textInputEditTextDeviceId = findViewById(R.id.device_id);
        switchMaterialNFC = findViewById(R.id.nfc_reader);
        switchMaterialQR = findViewById(R.id.qr_code);

        findViewById(R.id.btn_save_settings).setOnClickListener(v -> onSaveSettings());

        findViewById(R.id.btn_home).setOnClickListener(v -> {
            if (!Objects.requireNonNull(viewModel.getAllConfigurations().getValue()).isEmpty())
                finish();
        });

        viewModel.getAllConfigurations().observe(this, this::setNewValues);

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

    private void onSaveSettings() {
        List<Configuration> configurations = new ArrayList<>();
        configurations.add(new Configuration("url_base", String.valueOf(textInputEditTextUrlBase.getText())));
        configurations.add(new Configuration("url_image", String.valueOf(textInputEditTextUrlImage.getText())));
        configurations.add(new Configuration("seconds_in_green", String.valueOf(textInputEditTextSecondsInGreen.getText())));
        configurations.add(new Configuration("seconds_in_red", String.valueOf(textInputEditTextSecondsInRed.getText())));
        configurations.add(new Configuration("device_id", String.valueOf(textInputEditTextDeviceId.getText())));
        configurations.add(new Configuration("nfc_status", switchMaterialNFC.isChecked() ? "ON" : "OFF"));
        configurations.add(new Configuration("qr_status", switchMaterialQR.isChecked() ? "ON" : "OFF"));
        viewModel.createOrUpdateConfigurations(SettingsActivity.this, configurations);
    }

    private void setNewValues(List<Configuration> configurations) {
        for (int index = 0; index < configurations.size(); index++) {
            switch (configurations.get(index).name) {
                case "url_base":
                    textInputEditTextUrlBase.setText(configurations.get(index).value);
                    break;
                case "url_image":
                    textInputEditTextUrlImage.setText(configurations.get(index).value);
                    break;
                case "seconds_in_green":
                    textInputEditTextSecondsInGreen.setText(configurations.get(index).value);
                    break;
                case "seconds_in_red":
                    textInputEditTextSecondsInRed.setText(configurations.get(index).value);
                    break;
                case "device_id":
                    textInputEditTextDeviceId.setText(configurations.get(index).value);
                    break;
                case "nfc_status":
                    switchMaterialNFC.setChecked(configurations.get(index).value.equals("ON"));
                    break;
                case "qr_status":
                    switchMaterialQR.setChecked(configurations.get(index).value.equals("ON"));
                    break;
            }
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
}