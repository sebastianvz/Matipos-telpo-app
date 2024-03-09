package com.example.telpoandroiddemo.infraestructure.devices;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.common.apiutil.ResultCode;
import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.util.SDKUtil;
import com.common.callback.IDecodeReaderListener;
import com.example.telpoandroiddemo.application.devices.TelpoDevices;

import java.nio.charset.StandardCharsets;

public class Telpo implements TelpoDevices {

    private DecodeReader decodeReader;
    private final Context context;
    private Boolean isQrEnable;

    private MutableLiveData<String> code;

    public Telpo(Context context) {
        this.isQrEnable = false;
        this.context = context;
        SDKUtil.getInstance(context).initSDK();
        startDecodeReader();
    }

    @Override
    public void startDecodeReader() {
        if (decodeReader == null)
            decodeReader = new DecodeReader(context);

        if (!isQrEnable) {
            try {
                isQrEnable = decodeReader.open(115200) == ResultCode.SUCCESS;
                if (isQrEnable) {
                    decodeReader.setDecodeReaderListener(new IDecodeReaderListener() {
                        @Override
                        public void onRecvData(byte[] bytes) {
                            if (bytes.length != 0) {
                                String strCode = new String(bytes, StandardCharsets.UTF_8);
                                code.postValue(strCode.replace("\r\n", ""));
                            }
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Boolean enableQrReader() {
        if (!isQrEnable)
            isQrEnable = decodeReader.open(115200) == ResultCode.SUCCESS;
        return isQrEnable;
    }

    @Override
    public DecodeReader getDecodeReaderObject() {
        return decodeReader;
    }

    @Override
    public void stopDecodeReader() {
        if (decodeReader != null) {
            decodeReader.close();
            isQrEnable = false;
        }
    }

    @Override
    public LiveData<String> codeData() {
        if (code == null)
            code = new MutableLiveData<String>();
        return code;
    }
}
