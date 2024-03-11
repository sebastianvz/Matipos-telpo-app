package com.example.telpoandroiddemo.application.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.example.telpoandroiddemo.domain.entities.RecordLog;

@Dao
public interface RecordLogsDao {
    @Insert
    void insert(RecordLog log);
}
