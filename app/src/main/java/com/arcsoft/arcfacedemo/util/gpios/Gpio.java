package com.arcsoft.arcfacedemo.util.gpios;

import android.content.Context;
import android.os.SystemClock;

import com.common.apiutil.led.Led;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gpio {

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void toggle(Context context, int type, int color, int timeout) {
        executorService.execute(() -> {
            Led led = new Led(context);
            try {
                led.setColorLed(type, color, 125);
                SystemClock.sleep(timeout);
                led.setColorLed(type, color,0);
            } catch (Exception ignored) {
            }
        });
    }

}
