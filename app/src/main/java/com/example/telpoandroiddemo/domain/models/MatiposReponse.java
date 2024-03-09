package com.example.telpoandroiddemo.domain.models;

import com.google.gson.annotations.SerializedName;

public class MatiposReponse {
    @SerializedName("Estado")
    private final Boolean status;
    @SerializedName("Direccion")
    private final String address;
    @SerializedName("Fecha")
    private final String date;
    @SerializedName("Respuesta")
    private final String ans;

    public MatiposReponse(Boolean status, String address, String date, String ans) {
        this.status = status;
        this.address = address;
        this.date = date;
        this.ans = ans;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }

    public String getAns() {
        return ans;
    }
}
