package com.example.telpoandroiddemo.viewmodels;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.common.callback.IDecodeReaderListener;
import com.example.telpoandroiddemo.application.devices.TelpoDevices;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;
import com.example.telpoandroiddemo.infraestructure.devices.Telpo;

import java.nio.charset.StandardCharsets;
import java.util.List;


public class MainViewModel extends ViewModel {

    private Application application;
    private TelpoDevices telpoDevices;
    private String macAddress;
    private LiveData<List<Configuration>> allConfigurations;

    public void startApplication(Application application) {
        this.application = application;
        ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        allConfigurations = repository.readAll();
        telpoDevices = Telpo.getInstance(application.getApplicationContext());
    }

    public LiveData<List<Configuration>> getAllConfigurations() {
        if (allConfigurations == null)
            allConfigurations = new MutableLiveData<List<Configuration>>();
        return allConfigurations;
    }

    public TelpoDevices getDevice() {
        return telpoDevices;
    }

    public LiveData<String> getCodeData() {
        return telpoDevices.codeData();
    }

    public String getConfiguration(String key) {
        ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        return repository.readByName(key).value;
    }

    public String getMacAddress() {
        Context context = application.getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        @SuppressLint("HardwareIds") String macAddress = wifiManager.getConnectionInfo().getMacAddress();
        if (TextUtils.isEmpty(macAddress))
            macAddress = "Device doesn't have mac address or wi-fi is disabled";
        return macAddress;
    }

    public String getLogo() {
        ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        return repository.readByName("logo").value;
    }

}
