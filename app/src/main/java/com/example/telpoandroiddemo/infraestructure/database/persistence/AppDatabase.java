package com.example.telpoandroiddemo.infraestructure.database.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.telpoandroiddemo.application.dao.ConfigurationDao;
import com.example.telpoandroiddemo.application.dao.RecordLogsDao;
import com.example.telpoandroiddemo.application.dao.UserDao;
import com.example.telpoandroiddemo.domain.entities.Configuration;
import com.example.telpoandroiddemo.domain.entities.RecordLog;
import com.example.telpoandroiddemo.domain.entities.User;

@Database(entities = {Configuration.class, RecordLog.class, User.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new table with the new default value
            database.execSQL(
                    "CREATE TABLE new_users (" +
                            "id INTEGER PRIMARY KEY NOT NULL, " +
                            "username TEXT, " +
                            "password TEXT, " +
                            "created_at TEXT NOT NULL)"
            );

            // Copy the data
            database.execSQL(
                    "INSERT INTO new_users (id, username, password, created_at) " +
                            "SELECT id, username, password, created_at FROM users"
            );

            // Remove the old table
            database.execSQL("DROP TABLE users");

            // Change the table name to the original one
            database.execSQL("ALTER TABLE new_users RENAME TO users");
        }
    };
    public static AppDatabase getInstance(Context context) {
        if (instance == null)
        {
            instance = Room.databaseBuilder(context, AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }

    public abstract ConfigurationDao configurationDao();
    public abstract RecordLogsDao recordLogsDao();
    public abstract UserDao userDao();
}