package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.matiposserver.MatiposServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatiposViewModel extends ViewModel {

    private MatiposServer matiposServer;
    private MutableLiveData<Boolean> processEnding;

    public void postValidationCode(Context context, String code, String macAddress) {
        if (matiposServer == null)
            matiposServer = new MatiposServer();

        matiposServer.run(context, code, macAddress);
    }

    public LiveData<MatiposResponseServer> getMatiposResponse() {
        if (matiposServer == null)
            matiposServer = new MatiposServer();

        return matiposServer.getMatiposResponseServer();
    }

    public void startProcessToEndingValidateCode(MediaPlayer mediaPlayer, int seconds) {
        new AsyncEvent().execute(mediaPlayer, seconds);
    }

    public LiveData<Boolean> IsProcessEnding() {
        if (processEnding == null)
            processEnding = new MutableLiveData<>();

        return processEnding;
    }


    // Async response validate code
    private class AsyncEvent {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void execute(MediaPlayer mediaPlayerInfoMessage, int seconds) {
            executorService.execute(() -> {

                processEnding.postValue(false);

                int timeout = seconds;
                timeout /= 1000;

                int secondsCounter = 0;
                while (mediaPlayerInfoMessage.isPlaying() || secondsCounter < timeout) {
                    SystemClock.sleep(1000);
                    secondsCounter++;
                }

                handler.post(() -> {
                    if (processEnding == null)
                        processEnding = new MutableLiveData<>();

                    processEnding.postValue(true);
                });
            });
        }
    }

    public void updateFaceIdInMovement(Context context, int movementId) {
        new AsyncUpdateFaceId().execute(context, movementId);
    }

    private class AsyncUpdateFaceId {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        public void execute(Context context, int movementId) {
            executorService.execute(() -> {
                matiposServer.updateFaceIdByMovementById(context, movementId);
            });
        }
    }

}
