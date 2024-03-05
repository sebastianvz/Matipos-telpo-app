package com.example.telpoandroiddemo.application.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.telpoandroiddemo.domain.entities.Configuration;

import java.util.List;

@Dao
public interface ConfigurationDao {

    @Insert
    void insert(Configuration configuration);
    @Update
    void update(Configuration configuration);

    @Query("SELECT * FROM Configuration WHERE name = :name")
    Configuration readByName(String name);

    @Query("SELECT * FROM Configuration ORDER BY uid DESC")
    LiveData<List<Configuration>> readAll();

}
