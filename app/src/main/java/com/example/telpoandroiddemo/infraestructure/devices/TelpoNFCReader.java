package com.example.telpoandroiddemo.infraestructure.devices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.common.CommonConstants;
import com.common.apiutil.CommonException;
import com.common.apiutil.led.Led;
import com.common.apiutil.nfc.Nfc;
import com.common.apiutil.util.SDKUtil;
import com.example.telpoandroiddemo.application.devices.ITelpoNFCReader;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelpoNFCReader implements ITelpoNFCReader {
    @SuppressLint("StaticFieldLeak")
    private static TelpoNFCReader instance;
    private Nfc nfc;
    private final Context context;
    private Boolean isEnable;

    private MutableLiveData<String> uid;

    public static TelpoNFCReader getInstance(Context context) {
        if (instance == null)
            instance = new TelpoNFCReader(context);
        return instance;
    }

    public TelpoNFCReader(Context context) {
        this.context = context;
        isEnable = false;
        SDKUtil.getInstance(context).initSDK();
        start();
    }

    @Override
    public LiveData<String> getNFCCode() {
        if (uid == null)
            uid = new MutableLiveData<>();
        return uid;
    }

    @Override
    public void start() {
        if (nfc == null)
            nfc = new Nfc(context);

        if (!isEnable) {
            try {
                isEnable = true;
                nfc.open();
                Led currentLed = new Led(context);
                currentLed.setColorLed(CommonConstants.LedType.COLOR_LED_1, CommonConstants.LedColor.GREEN_LED,255);
                new NFCThreadReader().run();
            } catch (CommonException ignored) {}
        }
    }

    @Override
    public void stop() {
        try {
            isEnable = false;
            nfc.close();
            Led currentLed = new Led(context);
            currentLed.setColorLed(CommonConstants.LedType.COLOR_LED_1, CommonConstants.LedColor.GREEN_LED,0);
        } catch (CommonException ignored) {}
    }

    private class NFCThreadReader {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void run() {
            executorService.execute(() -> {
                String uidString = "";
                while (isEnable) {
                    byte[] outData = new byte[256];
                    int[] outLen = new int[1];
                    int rc = checkNfc(outData, outLen);
                    if (rc > 0) {
                        byte[] buf = new byte[outLen[0]];
                        System.arraycopy(outData, 0, buf, 0, buf.length);
                        uidString = parseData(buf);
                        isEnable = false;
                        break;
                    }
                }
                String finalCode = uidString;
                handler.post(() -> {
                    if (finalCode.length() > 0)
                        uid.postValue(finalCode);
                });
            });
        }

        public int checkNfc(byte[] outData, int[] outLen) {
            byte[] data = new byte[0];
            int ans = 0;
            try {
                data = nfc.activate(1000);
                outLen[0] = data.length;
                System.arraycopy(data, 0, outData,0, outLen[0]);
                ans = 1;
            } catch (CommonException ignored) {}
            return ans;
        }

        public String parseData(byte[] obj) {
            byte[] buf = obj;
            int[] strLen = {2};
            byte[] uid = new byte[7];
            System.arraycopy(buf, 6, uid, 0, uid.length);
            strLen[0] = 7;
            String uid_str = bytesToHexString(uid);
            return uid_str;
        }

        public final String bytesToHexString(byte[] bArray) {
            StringBuffer sb = new StringBuffer(bArray.length);
            String sTemp;
            for (int i = 0; i < bArray.length; i++) {
                sTemp = Integer.toHexString(0xFF & bArray[i]);
                if (sTemp.length() < 2)
                    sb.append(0);
                sb.append(sTemp.toUpperCase());
            }
            return sb.toString();
        }
    }
}
