package com.example.telpoandroiddemo.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.telpoandroiddemo.domain.entities.RecordLog;
import com.example.telpoandroiddemo.infraestructure.database.repository.RecordLogRepository;

import java.util.List;

public class AuditViewModel extends ViewModel {

    public LiveData<List<RecordLog>> getRecordLogs(Context context) {
        return new RecordLogRepository().getRecordLogs(context);
    }

    public void purgeTable(Context context) {
        new RecordLogRepository().purgeTable(context);
    }
}
