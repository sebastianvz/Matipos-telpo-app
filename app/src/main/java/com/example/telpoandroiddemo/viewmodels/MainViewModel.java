package com.example.telpoandroiddemo.viewmodels;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.telpoandroiddemo.application.devices.TelpoDevices;
import com.example.telpoandroiddemo.application.useCases.GetLogoUseCase;
import com.example.telpoandroiddemo.application.useCases.ValidateCodeUseCase;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;
import com.example.telpoandroiddemo.infraestructure.devices.Telpo;

import java.util.List;


public class MainViewModel extends ViewModel {
    private LiveData<List<Configuration>> allConfigurations;
    private ValidateCodeUseCase validateCodeUseCase;

    public LiveData<List<Configuration>> getAllConfigurations(Context context) {
        if (allConfigurations == null) {
            ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(context));
            allConfigurations = repository.readAll();
        }
        return allConfigurations;
    }

    public TelpoDevices getDevice(Context context) {
        return Telpo.getInstance(context);
    }

    public LiveData<String> getCodeData(Context context) {
        return Telpo.getInstance(context).codeData();
    }

    public LiveData<String> GetLogo(Context context) {
        return GetLogoUseCase.execute(context);
    }

    public void ValidateCode(Context context, String code) {
        validateCodeUseCase.execute(context, code);
    }

    public LiveData<MatiposReponse> ValidateCodeResponse() {
        if (validateCodeUseCase == null)
            validateCodeUseCase = new ValidateCodeUseCase();
        return validateCodeUseCase.getReponseMutableLiveData();
    }

}
