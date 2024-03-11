package com.example.telpoandroiddemo.viewmodels;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.infraestructure.database.repository.ConfigurationRepository;

import java.util.List;

public class SettingsViewModel extends ViewModel {

    private final AppDatabase db;
    private LiveData<List<Configuration>> allConfigurations;

    public SettingsViewModel(Application application) {
        db = AppDatabase.getInstance(application.getApplicationContext());
        ConfigurationRepository repository = new ConfigurationRepository(db);
        allConfigurations = repository.readAll();
    }

    public void createOrUpdateConfigurations(Context context, List<Configuration> configurations) {
        // repository.createOrUpdateConfiguration(configurations);
        ConfigurationRepository repository = new ConfigurationRepository(db);
        new SaveDataTask(context, repository).execute(configurations);
    }

    public LiveData<List<Configuration>> getAllConfigurations() {
        if (allConfigurations == null)
            allConfigurations = new MutableLiveData<List<Configuration>>();
        return allConfigurations;
    }

    public class SaveDataTask extends AsyncTask<List<Configuration>, Void, Boolean> {
        private final Context context;
        private final ConfigurationRepository repository;

        public SaveDataTask(Context context, ConfigurationRepository repository) {
            this.context = context;
            this.repository = repository;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(List<Configuration>... lists) {
            List<Configuration> configurations = lists[0];
            repository.createOrUpdateConfiguration(configurations);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(context, "Save configurations successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
