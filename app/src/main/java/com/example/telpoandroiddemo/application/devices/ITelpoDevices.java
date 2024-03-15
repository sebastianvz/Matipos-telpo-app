package com.example.telpoandroiddemo.application.devices;

import androidx.lifecycle.LiveData;

import com.common.apiutil.decode.DecodeReader;

public interface ITelpoDevices {
    public void startDecodeReader();
    public Boolean enableQrReader();
    public DecodeReader getDecodeReaderObject();
    public void stopDecodeReader();
    public LiveData<String> codeData();
}
