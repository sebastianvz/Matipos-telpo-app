package com.example.telpoandroiddemo.domain.models;

import androidx.annotation.NonNull;

public class MatiposRequest {
    private final String entryCode;
    private final String macAddress;
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

    @NonNull
    @Override
    public String toString() {
        return "{\r\n" + //
                "  \"entryCode\": \"" + entryCode + "\",\r\n" + //
                "  \"macAddress\": \"" + macAddress + "\",\r\n" + //
                "  \"address\": \"" + address + "\"\r\n" + //
                "}";
    }
}

