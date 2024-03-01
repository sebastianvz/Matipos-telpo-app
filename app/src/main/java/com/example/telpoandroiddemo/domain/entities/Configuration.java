package com.example.telpoandroiddemo.domain.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Configuration {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "value")
    public String value;

    @ColumnInfo(name = "updated_at")
    public String updatedAt;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    public Configuration(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
