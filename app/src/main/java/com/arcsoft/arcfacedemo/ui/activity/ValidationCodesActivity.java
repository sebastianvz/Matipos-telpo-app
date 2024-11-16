package com.arcsoft.arcfacedemo.ui.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.ui.viewmodel.MatiposViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.gpios.Gpio;
import com.common.CommonConstants;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ValidationCodesActivity extends BaseActivity {

    private static final String TAG = "ValidationCodesActivity";
    private MediaPlayer mediaPlayerInfoMessage;
    private AlertDialog dialog;
    private MatiposViewModel matiposViewModel;
    private boolean inProgres = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_codes);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(attributes);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        initViewModels();

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            // finish();

            AlertDialog.Builder builder = new AlertDialog.Builder(ValidationCodesActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_custom_login, null);
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(view);

            builder.setTitle("Ir a menu aplicación");

            // Boton OK
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TextInputEditText username = view.findViewById(R.id.username);
                    TextInputEditText password = view.findViewById(R.id.password);

                    if (Objects.requireNonNull(username.getText()).toString().equals(ConfigUtil.getAdminUsername(getApplicationContext()))
                            && Objects.requireNonNull(password.getText()).toString().equals(ConfigUtil.getAdminPassword(getApplicationContext())))
                        finish();
                    else
                        showToast("Usuario o Contraseña incorrecto");
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog = builder.create();
            dialog.show();

        });

        findViewById(R.id.main_layout).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // When button is pressed
                    mediaPlayerInfoMessage = MediaPlayer.create(ValidationCodesActivity.this, R.raw.info);
                    mediaPlayerInfoMessage.start();
                    break;

                case MotionEvent.ACTION_UP: // When button is released
                    if (mediaPlayerInfoMessage != null) {
                        mediaPlayerInfoMessage.stop();
                        mediaPlayerInfoMessage.release();
                        mediaPlayerInfoMessage = null;
                    }
                    break;
            }
            return true;
        });

    }

    private void initViewModels() {

        matiposViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(MatiposViewModel.class);

        matiposViewModel.getMatiposResponse().observe(this, response -> {

            if (!inProgres)
                return;

            if (dialog != null)
                dialog.dismiss();

            // Star led module
            Gpio gpio = new Gpio();
            int ledColor = CommonConstants.LedColor.WHITE_LED;
            int ledSecondsInOn = ConfigUtil.getMatiposSecondsEnableLed(ValidationCodesActivity.this) * 1000;

            // Show dialog with information
            AlertDialog.Builder builder = new AlertDialog.Builder(ValidationCodesActivity.this);
            LayoutInflater inflater = ValidationCodesActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_matipos_server_response, null);
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            builder.setView(view);
            builder.setCancelable(false);
            LinearLayout linearLayout = view.findViewById(R.id.validate_code_container);

            if (mediaPlayerInfoMessage != null) {
                mediaPlayerInfoMessage.stop();
                mediaPlayerInfoMessage.release();
                mediaPlayerInfoMessage = null;
            }

            // Message information
            if (response.getStatus() != null) {
                TextView textView = linearLayout.findViewById(R.id.title);
                textView.setText(response.getAns());
                ledColor = response.getStatus() ? CommonConstants.LedColor.GREEN_LED : CommonConstants.LedColor.RED_LED;
                linearLayout.setBackgroundResource(response.getStatus() ? R.drawable.ok : R.drawable.no);

                int audioId = ConfigUtil.isInputDevice(ValidationCodesActivity.this) ? R.raw.ok : R.raw.ok_salida;
                mediaPlayerInfoMessage = MediaPlayer.create(ValidationCodesActivity.this, response.getStatus() ? audioId : R.raw.no);
            } else {
                linearLayout.setBackgroundResource(R.drawable.warning);
                TextView textView = linearLayout.findViewById(R.id.title);
                textView.setText(response.getAns());
                mediaPlayerInfoMessage = MediaPlayer.create(ValidationCodesActivity.this, R.raw.warning);
            }

            // Turn ON Led
            mediaPlayerInfoMessage.start();
            gpio.toggle(ValidationCodesActivity.this, CommonConstants.LedType.FILL_LIGHT_1, ledColor, ledSecondsInOn);

            dialog = builder.create();
            dialog.show();

            matiposViewModel.startProcessToEndingValidateCode(mediaPlayerInfoMessage, ledSecondsInOn);
        });
        matiposViewModel.IsProcessEnding().observe(this, isEnd -> {
            if (isEnd) {
                if (dialog != null)
                    dialog.dismiss();

                inProgres = false;
                dialog = null;
                startReaders();
            }
        });

        if (matiposViewModel.readersOk(getApplicationContext()))
            startReaders();
        else {
            showLongToast("No hay dispositivos activos");
            finish();
        }

    }

    private void startReaders() {
        matiposViewModel.startReaders(ValidationCodesActivity.this, ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()), ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext()));

        if (ConfigUtil.isMatiposIsQrReaderEnable(getApplicationContext()))
            matiposViewModel.getQrValidationCode().observe(this, this::matiposServerValidaCode);

        if (ConfigUtil.isMatiposIsNfcReaderEnable(getApplicationContext()))
            matiposViewModel.getNfcValidationCode().observe(this, this::matiposServerValidaCode);
    }

    private void matiposServerValidaCode(String data) {
        if (data.length() > 4) {

            // Update dialog with message validate code with server
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            // Build dialog info
            AlertDialog.Builder builder = new AlertDialog.Builder(ValidationCodesActivity.this);
            LayoutInflater inflater = ValidationCodesActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_validate_code, null);
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            builder.setView(view);
            builder.setCancelable(false);
            LinearLayout linearLayout = view.findViewById(R.id.linea_layout);
            TextView title = view.findViewById(R.id.title);
            TextView message = view.findViewById(R.id.message);
            title.setText("Validating Code");
            message.setText(data);
            linearLayout.setBackgroundResource(R.drawable.rounded);
            dialog = builder.create();
            dialog.show();

            // Disable qrReader
            matiposViewModel.stopReaders(ValidationCodesActivity.this);

            inProgres = true;
            validateCode(ValidationCodesActivity.this, data);

        }
    }

    public void validateCode(Context context, String code) {
        matiposViewModel.postValidationCode(context, code, ConfigUtil.getMatiposDeviceCode(context));
    }

    @Override
    protected void onDestroy() {

        if (matiposViewModel != null)
            matiposViewModel.stopReaders(ValidationCodesActivity.this);

        super.onDestroy();
    }
}