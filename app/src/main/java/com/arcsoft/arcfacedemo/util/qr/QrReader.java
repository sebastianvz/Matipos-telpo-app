package com.arcsoft.arcfacedemo.util.qr;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.common.apiutil.ResultCode;
import com.common.apiutil.decode.DecodeReader;
import com.common.apiutil.util.SDKUtil;

import java.nio.charset.StandardCharsets;

public class QrReader {

    private static QrReader instance;
    private MutableLiveData<String> qrValue;
    private DecodeReader decodeReader;

    public static QrReader getInstance(Context context) {
        if (instance == null) {
            instance = new QrReader(context);
        }
        return instance;
    }

    public QrReader(Context context)
    {
        SDKUtil.getInstance(context).initSDK();
    }

    public void startDecodeReader(Context context)
    {
        if (decodeReader == null)
            decodeReader = new DecodeReader(context);

        if (decodeReader.open(115200) == ResultCode.SUCCESS)
        {
            decodeReader.setDecodeReaderListener(bytes -> {
                if (bytes.length != 0) {
                    String strCode = new String(bytes, StandardCharsets.UTF_8);
                    qrValue.postValue(strCode.replace("\r\n", ""));
                }
            });
        }
    }

    public void stopDecodeReader() {
        if (decodeReader != null) {
            decodeReader.close();
        }
    }

    public LiveData<String> getValue()
    {
        if (qrValue == null)
            qrValue = new MutableLiveData<>();

        return qrValue;
    }

}
