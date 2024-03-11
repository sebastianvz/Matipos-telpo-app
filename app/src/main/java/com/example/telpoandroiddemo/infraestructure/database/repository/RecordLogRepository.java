package com.example.telpoandroiddemo.infraestructure.database.repository;

import android.content.Context;

import com.example.telpoandroiddemo.application.dao.RecordLogsDao;
import com.example.telpoandroiddemo.domain.entities.RecordLog;
import com.example.telpoandroiddemo.infraestructure.database.persistence.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordLogRepository {

    public void InsertLog(Context context, RecordLog log) {
        new InsertLogAsyncTask().execute(context, log);
    }

    private static class InsertLogAsyncTask {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        public void execute(Context context, RecordLog log) {
            executorService.execute(() -> {
                RecordLogsDao dao = AppDatabase.getInstance(context).recordLogsDao();
                dao.insert(log);
            });
        }
    }
}
