package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.common.CommonConstants;
import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.application.devices.ITelpoRGBLeds;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.infraestructure.devices.TelpoRGBLeds;
import com.example.telpoandroiddemo.viewmodels.MainViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ImageView imageView;
    private AlertDialog dialog;
    private Boolean qrStatus = false;
    private Boolean nfcStatus = false;
    private int secondsRed = 1000;
    private int secondsGreen = 500;

    // TODO: Clear
    int counter = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the navigation bar and status bar
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        imageView = findViewById(R.id.image_view);

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.main_layout).setOnClickListener(v -> {
            counter++;
            if (counter > 10) {
                Toast.makeText(MainActivity.this, "Task, Get new Logo programming", Toast.LENGTH_SHORT).show();
                counter = 0;
                viewModel.refreshLogo(MainActivity.this);
            }
        });

        viewModel.getAllConfigurations(MainActivity.this).observe(this, configurations -> {
            if (configurations.isEmpty()) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            else {
                 for (Configuration configuration : configurations) {
                     switch (configuration.name) {
                         case "qr_status":
                             qrStatus = configuration.value.equals("ON");
                             if (qrStatus)
                                 viewModel.getDevice(MainActivity.this).startDecodeReader();
                             else
                                 viewModel.getDevice(MainActivity.this).stopDecodeReader();
                             break;
                         case "nfc_status":
                             nfcStatus = configuration.value.equals("ON");
                             if (nfcStatus)
                                 viewModel.starNFCReader(MainActivity.this);
                             else
                                 viewModel.stopNFCReader(MainActivity.this);
                             break;
                         case "seconds_in_green":
                             secondsGreen = Integer.parseInt(configuration.value) * 1000;
                             break;
                         case "seconds_in_red":
                             secondsRed = Integer.parseInt(configuration.value) * 1000;
                             break;
                     }
                 }
            }
        });

        viewModel.getCodeData(MainActivity.this).observe(this, s -> {
            if (s.length() > 1) {
                // Stop qrReader
                viewModel.getDevice(MainActivity.this).stopDecodeReader();

                // Stop NFCReader
                viewModel.stopNFCReader(MainActivity.this);

                // Build dialog info
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.validate_code_dialog, null);
                view.setSystemUiVisibility(uiOptions);
                builder.setView(view);
                builder.setCancelable(false);
                LinearLayout linearLayout = view.findViewById(R.id.linea_layout);
                TextView title = view.findViewById(R.id.title);
                TextView message = view.findViewById(R.id.message);
                title.setText("Validating");
                message.setText(s);
                linearLayout.setBackgroundResource(R.drawable.rounded);
                dialog = builder.create();
                dialog.show();

                // Go to Server
                viewModel.ValidateCode(MainActivity.this, s);
            }
        });

        viewModel.ValidateCodeResponse().observe(this, matiposReponse -> {
            dialog.dismiss();

            // Led
            ITelpoRGBLeds rgbLeds = new TelpoRGBLeds();
            int ledColor = CommonConstants.LedColor.WHITE_LED;
            int ledSeconds = secondsRed;

            // Build Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.custom_dialog, null);
            view.setSystemUiVisibility(uiOptions);
            builder.setView(view);
            builder.setCancelable(false);
            LinearLayout linearLayout = view.findViewById(R.id.linea_layout);
            TextView title = view.findViewById(R.id.title);
            TextView message = view.findViewById(R.id.message);
            if (matiposReponse != null) {
                ledColor = matiposReponse.getStatus() ? CommonConstants.LedColor.GREEN_LED : CommonConstants.LedColor.RED_LED;
                ledSeconds = matiposReponse.getStatus() ? secondsGreen : secondsRed;
                linearLayout.setBackgroundResource(matiposReponse.getStatus() ? R.drawable.ok : R.drawable.no);
            }
            else {
                linearLayout.setBackgroundResource(R.drawable.warning);
            }

            // Turn ON Led
            rgbLeds.toggle(MainActivity.this, CommonConstants.LedType.FILL_LIGHT_1, ledColor, ledSeconds);

            dialog = builder.create();
            dialog.show();
            new Dialog().execute(matiposReponse);
        });

        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            if (packageInfo != null) {
                TextView versionApp = findViewById(R.id.versionApp);
                versionApp.setText(packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException ignore) {}

        viewModel.GetLogo(MainActivity.this).observe(this, encodedString -> {
            if (encodedString != null) {
                encodedString = encodedString.replace("\"","");
                byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                imageView.setImageBitmap(bitmap);
            }
            else  {
                Toast.makeText(MainActivity.this, "LogoBase65 is empty", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNfcCode(MainActivity.this).observe(this, s -> {
            // Stop qrReader
            viewModel.getDevice(MainActivity.this).stopDecodeReader();

            // Stop NFCReader
            viewModel.stopNFCReader(MainActivity.this);

            // Build dialog info
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.validate_code_dialog, null);
            view.setSystemUiVisibility(uiOptions);
            builder.setView(view);
            builder.setCancelable(false);
            LinearLayout linearLayout = view.findViewById(R.id.linea_layout);
            TextView title = view.findViewById(R.id.title);
            TextView message = view.findViewById(R.id.message);
            title.setText("Validating");
            message.setText(s);
            linearLayout.setBackgroundResource(R.drawable.rounded);
            dialog = builder.create();
            dialog.show();

            // Go to Server
            viewModel.ValidateCode(MainActivity.this, s);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        counter = 0;

        // QR Reader
        if (qrStatus)
            viewModel.getDevice(MainActivity.this).startDecodeReader();
        else
            viewModel.getDevice(MainActivity.this).stopDecodeReader();

        // NFC Reader
        if (nfcStatus)
            viewModel.starNFCReader(MainActivity.this);
        else
            viewModel.stopNFCReader(MainActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getDevice(MainActivity.this).stopDecodeReader();
        viewModel.stopNFCReader(MainActivity.this);
        counter = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getDevice(MainActivity.this).stopDecodeReader();
        viewModel.stopNFCReader(MainActivity.this);
        counter = 0;
    }

    private class Dialog {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        public void execute(MatiposReponse matiposReponse) {
            executorService.execute(() -> {
                // TODO:
                if (matiposReponse != null) {
                    SystemClock.sleep(matiposReponse.getStatus() ? secondsGreen : secondsRed);
                }
                else {
                    SystemClock.sleep(secondsRed);
                }
                handler.post(() -> {
                    dialog.dismiss();
                    if (qrStatus)
                        viewModel.getDevice(MainActivity.this).startDecodeReader();

                    if (nfcStatus)
                        viewModel.starNFCReader(MainActivity.this);
                });
            });
        }
    }

}