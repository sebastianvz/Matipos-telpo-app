package com.arcsoft.arcfacedemo.facedb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.dao.MovementDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;

import java.io.File;

@Database(entities = {FaceEntity.class, MovementEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FaceDao faceDao();
    public abstract MovementDao movementDao();

    private static volatile AppDatabase faceDatabase = null;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("CREATE TABLE `movement` (`movementId` INTEGER, "
                    + "`face_id` INTEGER, "
                    + "`operation_type` VARCHAR(255) NOT NULL, "
                    + "`request_data` TEXT NOT NULL, "
                    + "`request_datetime` VARCHAR(255) NOT NULL, "
                    + "`response_data` TEXT, "
                    + "`response_datetime` VARCHAR(255), "
                    + "PRIMARY KEY(`movementId`))");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (faceDatabase == null) {
            synchronized (AppDatabase.class) {
                if (faceDatabase == null) {
                    faceDatabase = Room.databaseBuilder(context, AppDatabase.class,
                            context.getExternalFilesDir("database") + File.separator + "faceDB.db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return faceDatabase;
    }
}
