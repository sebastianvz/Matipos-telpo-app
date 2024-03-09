package com.example.telpoandroiddemo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.example.telpoandroiddemo.R;
import com.example.telpoandroiddemo.application.services.IMatiposService;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;
import com.example.telpoandroiddemo.infraestructure.services.MatiposService;
import com.example.telpoandroiddemo.viewmodels.MainViewModel;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ProgressDialog progressDialog;
    private ImageView imageView;
    MutableLiveData<String> logo = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hideSystemUI();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
            viewModel.startApplication(getApplication());
        }

        imageView = (ImageView) findViewById(R.id.image_view);

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

        findViewById(R.id.image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Search");
                progressDialog.setMessage("Validating code ");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                SendCommand sendCommand = new SendCommand();
                sendCommand.execute("ABC123");
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

        logo.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String encodedString) {
                encodedString = encodedString.replace("\"","");
                byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                imageView.setImageBitmap(bitmap);
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

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getDevice().startDecodeReader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getDevice().stopDecodeReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getDevice().stopDecodeReader();
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
                    IMatiposService service = MatiposService.getInstance(viewModel.getConfiguration("url_base"));
                    MatiposRequest request = new MatiposRequest(code, viewModel.getMacAddress(), null);

                    MatiposReponse response = null;
                    String errorMessage = null;
                    String base64String = null;

                    try {
                        response = service.sendPutRequest(viewModel.getConfiguration("url_base"), request);
                        base64String = service.getImageInBase64(viewModel.getConfiguration("url_image"));
                    } catch (SocketTimeoutException e) {
                        errorMessage = e.getMessage();
                    }

                    // Put code OnPost here
                    MatiposReponse finalResponse = response;
                    String finalErrorMessage = errorMessage;
                    String finalBase64String = base64String;

                    logo.postValue(finalBase64String);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (finalResponse != null)
                                if (finalResponse.getStatus())
                                    Toast.makeText(getApplicationContext(), "Code Enable", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Code Disable", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), finalErrorMessage, Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });
        }
    }
}