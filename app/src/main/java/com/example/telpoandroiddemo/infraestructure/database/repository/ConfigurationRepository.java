package com.example.telpoandroiddemo.infraestructure.database.repository;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;
import com.example.telpoandroiddemo.application.dao.ConfigurationDao;
import com.example.telpoandroiddemo.domain.entities.Configuration;

import java.time.LocalDateTime;
import java.util.List;

public class ConfigurationRepository {

    private final AppDatabase db;

    public ConfigurationRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<List<Configuration>> readAll() {
        return db.configurationDao().readAll();
    }

    // Create or update
    public void createOrUpdateConfiguration(List<Configuration> configurations) {
        new AsyncCreateOrUpdateConfigurations(db.configurationDao()).execute(configurations);
    }

    static class AsyncCreateOrUpdateConfigurations extends AsyncTask<List<Configuration>, Void, Void> {
        private final ConfigurationDao configurationDao;

        public AsyncCreateOrUpdateConfigurations(ConfigurationDao configurationDao) {
            this.configurationDao = configurationDao;
        }

        @Override
        protected Void doInBackground(List<Configuration>... lists) {
            List<Configuration> configurations = lists[0];
            for (int index = 0; index < configurations.size(); index++) {
                Log.d("APP-LP", "onSaveSettings -> name: " + configurations.get(index).name + " value: " +  configurations.get(index).value);
                Configuration configuration = configurationDao.readByName(configurations.get(index).name);
                LocalDateTime dateTime = LocalDateTime.now();
                if (configuration == null) {
                    configurations.get(index).createdAt = dateTime.toString();
                    configurations.get(index).updatedAt = dateTime.toString();
                    configurationDao.insert(configurations.get(index));
                }
                else {
                    configurations.get(index).uid = configuration.uid;
                    configurations.get(index).createdAt = configuration.createdAt;
                    configurations.get(index).updatedAt = dateTime.toString();
                    configurationDao.update(configurations.get(index));
                }
            }
            return null;
        }
    }

}
