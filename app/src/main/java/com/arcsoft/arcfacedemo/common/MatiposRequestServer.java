package com.arcsoft.arcfacedemo.common;

import androidx.annotation.NonNull;

public class MatiposRequestServer {
    private final String entryCode;
    private final String macAddress;
    private final String address;

    public MatiposRequestServer(String entryCode, String macAddress, String address) {
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
                "  \"code\": \"" + entryCode + "\",\r\n" + //
                "  \"macAddress\": \"" + macAddress + "\",\r\n" + //
                "  \"direction\": \"" + address + "\"\r\n" + //
                "}";
    }
}
