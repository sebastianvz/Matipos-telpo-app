package com.example.telpoandroiddemo.application.devices;

import androidx.lifecycle.LiveData;

public interface ITelpoNFCReader {

    void start();
    LiveData<String> getNFCCode();

    void stop();
}
