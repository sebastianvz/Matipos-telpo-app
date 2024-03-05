package com.example.telpoandroiddemo.viewmodels;
import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;

import java.util.List;


public class MainViewModel extends ViewModel {

    private LiveData<List<Configuration>> allConfigurations;

    public MainViewModel(Application application) {
        ConfigurationRepository configurationRepository = new ConfigurationRepository(AppDatabase.getInstance(application.getApplicationContext()));
        allConfigurations = configurationRepository.readAll();
    }

    public LiveData<List<Configuration>> getAllConfigurations() {
        if (allConfigurations == null)
            allConfigurations = new MutableLiveData<List<Configuration>>();
        return allConfigurations;
    }

}
