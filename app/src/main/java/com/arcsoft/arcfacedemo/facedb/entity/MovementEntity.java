package com.arcsoft.arcfacedemo.facedb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;

import org.json.JSONObject;

@Entity(
        tableName = "movement"
)
public class MovementEntity {
    @PrimaryKey(autoGenerate = true)
    public int movementId;

    @ColumnInfo(name = "face_id")
    public int faceId;

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

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getRequestDatetime() {
        return requestDatetime;
    }

    public void setRequestDatetime(String requestDatetime) {
        this.requestDatetime = requestDatetime;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getResponseDatetime() {
        return responseDatetime;
    }

    public void setResponseDatetime(String responseDatetime) {
        this.responseDatetime = responseDatetime;
    }

    public MatiposResponseServer parseResponse() {
        MatiposResponseServer reponse = null;
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            reponse = new MatiposResponseServer(
                    (Boolean) jsonObject.get("status"),
                    (String) jsonObject.get("address"),
                    (String) jsonObject.get("date"),
                    (String) jsonObject.get("ans")
            );
        } catch (Exception ignored) {}
        return reponse;
    }

    public MatiposRequestServer parseRequest() {
        MatiposRequestServer request = null;
        try {
            JSONObject jsonObject = new JSONObject(requestData);
            request = new MatiposRequestServer(
                    (String) jsonObject.get("code"),
                    (String) jsonObject.get("macAddress"),
                    (String) jsonObject.get("direction")
            );
        } catch (Exception ignored) {}
        return request;
    }
}
