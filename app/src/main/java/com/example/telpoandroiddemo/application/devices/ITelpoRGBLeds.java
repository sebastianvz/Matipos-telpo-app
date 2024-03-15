package com.example.telpoandroiddemo.application.devices;

import android.content.Context;

public interface ITelpoRGBLeds {
    void toggle(Context context, int ledType, int ledColor, int timeout);
}
