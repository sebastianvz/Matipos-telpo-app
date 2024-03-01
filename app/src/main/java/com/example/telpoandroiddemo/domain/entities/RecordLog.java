package com.example.telpoandroiddemo.domain.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RecordLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "operation_type")
    public String operationType;

    @ColumnInfo(name = "request_data")
    public String requestData;

    @ColumnInfo(name = "request_datetime")
    public String requestDatetime;

    @ColumnInfo(name = "response_data")
    public String responseData;

    @ColumnInfo(name = "response_datetime")
    public String responseDatetime;
}
