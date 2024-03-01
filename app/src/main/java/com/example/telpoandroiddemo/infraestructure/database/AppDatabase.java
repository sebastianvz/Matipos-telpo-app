package com.example.telpoandroiddemo.infraestructure.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.telpoandroiddemo.infraestructure.database.dao.ConfigurationDao;
import com.example.telpoandroiddemo.infraestructure.database.dao.RecordLogsDao;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.domain.entities.RecordLog;

@Database(entities = {Configuration.class, RecordLog.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public static AppDatabase getInstance(Context context) {
        if (instance == null)
        {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    "database-name").allowMainThreadQueries().build();
        }
        return instance;
    }

    public abstract ConfigurationDao configurationDao();
    public abstract RecordLogsDao recordLogsDao();
}