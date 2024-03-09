package com.example.telpoandroiddemo.domain.models;

import com.google.gson.annotations.SerializedName;

public class MatiposRequest {
    @SerializedName("codigoEntrada")
    private final String entryCode;
    @SerializedName("direccionMAC")
    private final String macAddress;
    @SerializedName("direccion")
    private final String address;

    public MatiposRequest(String entryCode, String macAddress, String address) {
        this.entryCode = entryCode;
        this.macAddress = macAddress;
        this.address = address;
    }

    public String getEntryCode() {
        return entryCode;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getAddress() {
        return address;
    }
}
