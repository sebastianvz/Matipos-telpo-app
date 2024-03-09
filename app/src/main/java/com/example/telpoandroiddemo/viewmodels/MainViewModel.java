package com.example.telpoandroiddemo.viewmodels;
import android.app.Application;

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
    public TelpoDevices telpoDevices;
    public LiveData<String> codeData;
    private LiveData<List<Configuration>> allConfigurations;

    public void startApplication(Application application) {
        this.application = application;
        ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        allConfigurations = repository.readAll();
        telpoDevices = new Telpo(application.getApplicationContext());
    }

    public LiveData<List<Configuration>> getAllConfigurations() {
        if (allConfigurations == null)
            allConfigurations = new MutableLiveData<List<Configuration>>();
        return allConfigurations;
    }

    public Boolean isQrEnable() {
        ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        return repository.readByName("qr_status").value.equals("ON");
    }

    public LiveData<String> getCodeData() {
        return telpoDevices.codeData();
    }

}
