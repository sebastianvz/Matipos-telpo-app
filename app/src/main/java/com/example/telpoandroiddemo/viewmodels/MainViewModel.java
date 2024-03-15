package com.example.telpoandroiddemo.viewmodels;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.telpoandroiddemo.application.devices.ITelpoDevices;
import com.example.telpoandroiddemo.application.devices.ITelpoNFCReader;
import com.example.telpoandroiddemo.application.useCases.GetLogoUseCase;
import com.example.telpoandroiddemo.application.useCases.ValidateCodeUseCase;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;
import com.example.telpoandroiddemo.infraestructure.devices.TelpoNFCReader;
import com.example.telpoandroiddemo.infraestructure.devices.TelpoQrReader;

import java.util.List;


public class MainViewModel extends ViewModel {
    private LiveData<List<Configuration>> allConfigurations;
    private ValidateCodeUseCase validateCodeUseCase;

    private ITelpoNFCReader nfcReader;
    public LiveData<List<Configuration>> getAllConfigurations(Context context) {
        if (allConfigurations == null) {
            ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(context));
            allConfigurations = repository.readAll();
        }
        return allConfigurations;
    }

    public ITelpoDevices getDevice(Context context) {
        return TelpoQrReader.getInstance(context);
    }

    public LiveData<String> getCodeData(Context context) {
        return TelpoQrReader.getInstance(context).codeData();
    }

    public LiveData<String> getNfcCode(Context context) {
        if (nfcReader == null)
            nfcReader = TelpoNFCReader.getInstance(context);
        return nfcReader.getNFCCode();
    }

    public void starNFCReader(Context context) {
        if (nfcReader == null)
            nfcReader = TelpoNFCReader.getInstance(context);
        nfcReader.start();
    }
    public void stopNFCReader(Context context) {
        if (nfcReader == null)
            nfcReader = TelpoNFCReader.getInstance(context);
        nfcReader.stop();
    }

    public LiveData<String> GetLogo(Context context) {
        return GetLogoUseCase.execute(context);
    }

    public void refreshLogo(Context context) {
        GetLogoUseCase.getLogoFromApi(context);
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
