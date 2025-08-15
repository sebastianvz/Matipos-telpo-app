package com.arcsoft.arcfacedemo.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class MatiposServerSettingsActivity extends BaseActivity {

    private TextInputEditText textInputEditTextUrlBase;
    private SwitchMaterial switchDeviceType;

    // Vars new function
    private TextInputEditText urlBaseFaceRepository;
    private SwitchMaterial switchOperationRepositoryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matipos_server_settings);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(attributes);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        initView();
    }

    @SuppressLint("HardwareIds")
    private void initView() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
        });

        // Input text
        textInputEditTextUrlBase = findViewById(R.id.urlBase);
        textInputEditTextUrlBase.setText(ConfigUtil.getMatiposUrlServer(MatiposServerSettingsActivity.this));

        switchDeviceType = findViewById(R.id.switchDeviceType);
        switchDeviceType.setChecked(ConfigUtil.isInputDevice(MatiposServerSettingsActivity.this));

        urlBaseFaceRepository = findViewById(R.id.urlBaseFaceRepository);
        urlBaseFaceRepository.setText(ConfigUtil.getUrlBaseFaceRepository(MatiposServerSettingsActivity.this));

        switchOperationRepositoryType = findViewById(R.id.switchOperationRepositoryType);
        switchOperationRepositoryType.setChecked(ConfigUtil.isOperationRepositoryType(MatiposServerSettingsActivity.this));

        TextView textViewDeviceSerial = findViewById(R.id.deviceSerial);
        textViewDeviceSerial.setText(
                Settings.Secure.getString(
                        this.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                )
        );

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(MatiposServerSettingsActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog, null);
            dialogView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(dialogView);

            // Optionally, set title and buttons
            builder.setTitle("Actualizar configuraciones");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean isError = true;

                    String urlBase = String.valueOf(textInputEditTextUrlBase.getText());
                    if (ConfigUtil.setMatiposUrlServer(getApplicationContext(), urlBase)) {
                        if (ConfigUtil.setInputDevice(getApplicationContext(), switchDeviceType.isChecked())) {
                            String urlBaseRepo = String.valueOf(urlBaseFaceRepository.getText());
                            if (ConfigUtil.setUrlBaseFaceRepository(getApplicationContext(), urlBaseRepo)) {
                                if (ConfigUtil.setOperationRepositoryType(getApplicationContext(), switchOperationRepositoryType.isChecked())) {
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
                    textInputEditTextUrlBase.setText(ConfigUtil.getMatiposUrlServer(MatiposServerSettingsActivity.this));
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }
}