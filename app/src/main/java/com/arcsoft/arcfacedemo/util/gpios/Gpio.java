package com.arcsoft.arcfacedemo.util.gpios;

import android.content.Context;
import android.os.SystemClock;

import com.common.CommonConstants;
import com.common.apiutil.led.Led;
import com.common.apiutil.pos.CommonUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gpio {

    private int type = 0;
    private int color = 0;
    private int brightness = 0;
    private Led led = null;

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void toggle(Context context, int type, int color, int timeout) {
        executorService.execute(() -> {
            if (led == null)
                led = new Led(context);
            else {
                try {
                    led.setColorLed(this.type, this.color,0);
                } catch (Exception ignored) {
                }
            }

            try {
                // Relay
                CommonUtil relay = new CommonUtil(context);
                relay.setRelayPower(color == CommonConstants.LedColor.GREEN_LED ? CommonConstants.RelayType.RELAY_1 : CommonConstants.RelayType.RELAY_2, 1);
                led.setColorLed(type, color, 125);
                SystemClock.sleep(timeout);
                led.setColorLed(type, color,0);
                relay.setRelayPower(color == CommonConstants.LedColor.GREEN_LED ? CommonConstants.RelayType.RELAY_1 : CommonConstants.RelayType.RELAY_2, 0);
                led.setColorLed(this.type, this.color,brightness);
            } catch (Exception ignored) {
            }
        });
    }

    public void write(Context context, int type, int color, int brightness) {

        off();

        if (led == null)
            led = new Led(context);

        this.type = type;
        this.color = color;
        this.brightness = brightness;
        try {
            led.setColorLed(type, color, brightness);
        } catch (Exception ignored) {
        }
    }

    public void off() {
        try {
            if (this.type != 0 && this.color != 0)
                led.setColorLed(this.type, this.color,0);

            this.type = 0;
            this.color = 0;
            this.brightness = 0;
        } catch (Exception ignored) {
        }
    }
}
