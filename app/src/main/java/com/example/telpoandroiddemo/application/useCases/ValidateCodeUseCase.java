package com.example.telpoandroiddemo.application.useCases;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.domain.entities.RecordLog;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;
import com.example.telpoandroiddemo.infraestructure.database.repository.RecordLogRepository;
import com.example.telpoandroiddemo.infraestructure.services.MatiposService;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidateCodeUseCase {

    static private MutableLiveData<MatiposReponse> reponseMutableLiveData;

    public ValidateCodeUseCase() {
    }

    public void execute(Context context, String code) {
        // Build request model
        // TODO: Read device MAC Address
        MatiposRequest request = new MatiposRequest(code, "MAC", "null");

        // Execute Task
        new AsyncValidateCodeUseCase().execute(context, request);
    }

    public MutableLiveData<MatiposReponse> getReponseMutableLiveData() {
        if (reponseMutableLiveData == null)
            reponseMutableLiveData = new MutableLiveData<>();
        return reponseMutableLiveData;
    }

    private static class AsyncValidateCodeUseCase {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void execute(Context context, MatiposRequest matiposRequest) {
            executorService.execute(() -> {

                RecordLog log = new RecordLog();
                log.operationType = "VALIDATE";
                log.requestData = matiposRequest.toString();
                log.requestDatetime = LocalDateTime.now().toString();

                MatiposReponse matiposReponse = null;
                ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(context));
                Configuration configuration = repository.readByName("url_base");
                if (configuration != null) {
                    try {
                        matiposReponse = MatiposService.getInstance().sendPutRequest(configuration.value, matiposRequest);
                    } catch (SocketTimeoutException ignored) {}
                }

                // TODO: Save log
                log.responseData = matiposReponse == null ? "NULL" : matiposReponse.toString();
                log.responseDatetime = LocalDateTime.now().toString();

                RecordLogRepository recordLogRepository = new RecordLogRepository();
                recordLogRepository.InsertLog(context, log);

                MatiposReponse finalMatiposReponse = matiposReponse;
                handler.post(() -> {
                    reponseMutableLiveData.postValue(finalMatiposReponse);
                });
            });
        }
    }

}
