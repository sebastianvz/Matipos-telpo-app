package com.example.telpoandroiddemo.domain.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;

import org.json.JSONObject;

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

    public MatiposReponse getResponseModel() {
        MatiposReponse reponse = null;
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            reponse = new MatiposReponse(
                    (Boolean) jsonObject.get("status"),
                    (String) jsonObject.get("address"),
                    (String) jsonObject.get("date"),
                    (String) jsonObject.get("ans")
            );
        } catch (Exception ignored) {}
        return reponse;
    }

    public MatiposRequest getRequestModel() {
        MatiposRequest request = null;
        try {
            JSONObject jsonObject = new JSONObject(requestData);
            request = new MatiposRequest(
                    (String) jsonObject.get("code"),
                    (String) jsonObject.get("macAddress"),
                    (String) jsonObject.get("direction")
            );
        } catch (Exception ignored) {}
        return request;
    }
}
