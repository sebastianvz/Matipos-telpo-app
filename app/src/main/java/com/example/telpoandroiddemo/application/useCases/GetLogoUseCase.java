package com.example.telpoandroiddemo.application.useCases;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;
import com.example.telpoandroiddemo.infraestructure.services.MatiposService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GetLogoUseCase {

    private static MutableLiveData<String> strLogoBase64 =  null;

    public static LiveData<String> execute(Context context) {
        if (strLogoBase64 == null) {
            // Create object
            strLogoBase64 = new MutableLiveData<>();

            // Get Logo in Base64 from database
            ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(context));
            Configuration logoConfiguration = repository.readByName("logo");
            Configuration urlImage = repository.readByName("url_image");

            if (logoConfiguration == null) {
                if (urlImage != null) {
                    if (urlImage.value.length() != 0)
                        AsyncTaskGetLogoUseCase.execute(context, urlImage.value);
                    else
                        Toast.makeText(context, "URL Image Configuration Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "URL Image Configuration Don't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (logoConfiguration.value == null) {
                    if (urlImage.value.length() != 0)
                        AsyncTaskGetLogoUseCase.execute(context, urlImage.value);
                    else
                        Toast.makeText(context, "URL Image Configuration Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    strLogoBase64.postValue(logoConfiguration.value);
                }
            }
        }

        return strLogoBase64;
    }

    private static class AsyncTaskGetLogoUseCase {
        static ExecutorService executorService = Executors.newSingleThreadExecutor();
        static Handler handler = new Handler(Looper.getMainLooper());

        public static void execute(Context context, String urlLogo) {
            executorService.execute(() -> {
                String base64String = null;
                try {
                    base64String = MatiposService.getInstance().getImageInBase64(urlLogo);
                    base64String = base64String.replace("\"","");
                } catch (Exception ignored) {}
                List<Configuration> logoConfigurations = new ArrayList<>();
                logoConfigurations.add(new Configuration("logo", base64String));
                ConfigurationRepository repository = new ConfigurationRepository(AppDatabase.getInstance(context));
                repository.createOrUpdateConfiguration(logoConfigurations);
                String finalBase64String = base64String;
                handler.post(() -> strLogoBase64.postValue(finalBase64String));
            });
        }
    }
}
