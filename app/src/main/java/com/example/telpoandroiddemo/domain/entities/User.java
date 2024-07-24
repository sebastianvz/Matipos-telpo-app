package com.example.telpoandroiddemo.domain.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo
    public String password;

    @ColumnInfo(name = "created_at")
    public String createdAt;

}
