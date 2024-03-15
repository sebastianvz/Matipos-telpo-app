package com.example.telpoandroiddemo.application.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.telpoandroiddemo.domain.entities.RecordLog;

import java.util.List;

@Dao
public interface RecordLogsDao {
    @Insert
    void insert(RecordLog log);

    @Query("SELECT * FROM RecordLog ORDER BY id DESC")
    LiveData<List<RecordLog>> getRecordLogs();

    @Query("DELETE FROM RecordLog")
    void purge();
}
