package com.example.telpoandroiddemo.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

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

        findViewById(R.id.btn_audit).setOnClickListener(v -> {
            Intent intent = new Intent(this, Audit.class);
            startActivity(intent);
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

        // Add OnEditorActionListener to texInput
        textInputEditTextUrlBase.setOnEditorActionListener((v, actionId, event) -> hideKeyBoard(v, event));
        textInputEditTextUrlImage.setOnEditorActionListener((v, actionId, event) -> hideKeyBoard(v, event));
        textInputEditTextSecondsInRed.setOnEditorActionListener((v, actionId, event) -> hideKeyBoard(v, event));
        textInputEditTextSecondsInGreen.setOnEditorActionListener((v, actionId, event) -> hideKeyBoard(v, event));
        textInputEditTextDeviceId.setOnEditorActionListener((v, actionId, event) -> hideKeyBoard(v, event));

    }

    private void onSaveSettings() {
        List<Configuration> configurations = new ArrayList<>();
        if (Objects.requireNonNull(textInputEditTextUrlBase.getText()).length() == 0
                || Objects.requireNonNull(textInputEditTextUrlImage.getText()).length() == 0
                || Objects.requireNonNull(textInputEditTextSecondsInRed.getText()).length() == 0
                || Objects.requireNonNull(textInputEditTextSecondsInGreen.getText()).length() == 0
                || Objects.requireNonNull(textInputEditTextDeviceId.getText()).length() == 0)
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        else {
            configurations.add(new Configuration("url_base", String.valueOf(textInputEditTextUrlBase.getText())));
            configurations.add(new Configuration("url_image", String.valueOf(textInputEditTextUrlImage.getText())));
            configurations.add(new Configuration("seconds_in_green", String.valueOf(textInputEditTextSecondsInGreen.getText())));
            configurations.add(new Configuration("seconds_in_red", String.valueOf(textInputEditTextSecondsInRed.getText())));
            configurations.add(new Configuration("device_id", String.valueOf(textInputEditTextDeviceId.getText())));
            configurations.add(new Configuration("nfc_status", switchMaterialNFC.isChecked() ? "ON" : "OFF"));
            configurations.add(new Configuration("qr_status", switchMaterialQR.isChecked() ? "ON" : "OFF"));
            viewModel.createOrUpdateConfigurations(SettingsActivity.this, configurations);

            finish();
        }
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

    private boolean hideKeyBoard(View v, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // Hide Keyboard
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return true;
        }
        return false;
    }
}