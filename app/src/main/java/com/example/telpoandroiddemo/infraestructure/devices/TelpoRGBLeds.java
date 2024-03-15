package com.example.telpoandroiddemo.infraestructure.devices;

import android.content.Context;
import android.os.SystemClock;

import com.common.CommonConstants;
import com.common.apiutil.CommonException;
import com.common.apiutil.led.Led;
import com.common.apiutil.pos.CommonUtil;
import com.example.telpoandroiddemo.application.devices.ITelpoRGBLeds;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelpoRGBLeds implements ITelpoRGBLeds {
    static ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    public void toggle(Context context, int ledType, int ledColor, int timeout) {
        executorService.execute(() -> {
            Led currentLed = new Led(context);
            CommonUtil relay = new CommonUtil(context);
            try {
                currentLed.setColorLed(ledType, ledColor,255);
                relay.setRelayPower(ledColor == CommonConstants.LedColor.RED_LED ? CommonConstants.RelayType.RELAY_1 : CommonConstants.RelayType.RELAY_2, 1);
                SystemClock.sleep(timeout);
                currentLed.setColorLed(ledType, ledColor,0);
                relay.setRelayPower(ledColor == CommonConstants.LedColor.RED_LED ? CommonConstants.RelayType.RELAY_1 : CommonConstants.RelayType.RELAY_2, 0);
            } catch (CommonException ignored) {}
        });
    }
}
