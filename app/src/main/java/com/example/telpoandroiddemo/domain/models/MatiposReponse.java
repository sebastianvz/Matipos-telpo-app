package com.example.telpoandroiddemo.domain.models;


import androidx.annotation.NonNull;

public class MatiposReponse {
    private final Boolean status;
    private final String address;
    private final String date;
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

    @NonNull
    @Override
    public String toString() {
        return "{\r\n" + //
                "  \"status\": \"" + status + "\",\r\n" + //
                "  \"address\": \"" + address + "\",\r\n" + //
                "  \"date\": \"" + date + "\"\r\n" + //
                "  \"ans\": \"" + ans + "\"\r\n" + //
                "}";
    }
}
