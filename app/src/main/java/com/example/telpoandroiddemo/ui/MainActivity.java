package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.viewmodels.MainViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ImageView imageView;
    private AlertDialog dialog;
    private Boolean qrStatus = false;
    private int secondsRed = 1000;
    private int secondsGreen = 500;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Hide the navigation bar and status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        imageView = findViewById(R.id.image_view);

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
                linearLayout.setBackgroundResource(matiposReponse.getStatus() ? R.drawable.ok : R.drawable.no);
                title.setText(matiposReponse.getStatus() ? "ENABLE" : "DISABLE");
                message.setText(matiposReponse.getAns());
            }
            else {
                title.setText("Error");
                message.setText("Error in communication with server");
                linearLayout.setBackgroundResource(R.drawable.warning);
            }

            dialog = builder.create();
            dialog.show();
            new Dialog().execute(matiposReponse);
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            startActivity(intent);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (qrStatus)
            viewModel.getDevice(MainActivity.this).startDecodeReader();
        else
            viewModel.getDevice(MainActivity.this).stopDecodeReader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getDevice(MainActivity.this).stopDecodeReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getDevice(MainActivity.this).stopDecodeReader();
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
                });
            });
        }
    }

}