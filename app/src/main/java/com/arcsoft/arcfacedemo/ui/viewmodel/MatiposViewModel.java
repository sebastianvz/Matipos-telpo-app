package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.matiposserver.MatiposServer;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.nfc.NfcReader;
import com.arcsoft.arcfacedemo.util.qr.QrReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatiposViewModel extends ViewModel {

    private MatiposServer matiposServer;
    private MutableLiveData<Boolean> processEnding;

    private QrReader qrReader;
    private NfcReader nfcReader;
    private MutableLiveData<String> qrValidationCode;
    private MutableLiveData<String> nfcValidationCode;

    public void startReaders(Context context, boolean isQrReaderEnable, boolean isNfcReaderEnable) {
        // Start qrReader
        if (qrReader == null) {
            qrReader = QrReader.getInstance(context);
        }

        if (isQrReaderEnable) {
            qrReader.startDecodeReader(context);
            qrValidationCode = (MutableLiveData<String>) qrReader.getValue();
        } else {
            qrReader.stopDecodeReader();
        }

        if (nfcReader == null)
            nfcReader = NfcReader.getInstance(context);

        if (isNfcReaderEnable) {
            nfcReader.start();
            nfcValidationCode = (MutableLiveData<String>) nfcReader.getNFCCode();
        } else {
            NfcReader.getInstance(context).stop();
        }
    }

    public void stopReaders(Context context) {

        nfcValidationCode = null;
        qrValidationCode = null;

        if (qrReader != null) {
            qrReader.stopDecodeReader();
        }
        NfcReader.getInstance(context).stop();
    }

    public void postValidationCode(Context context, String code, String macAddress) {
        if (matiposServer == null) matiposServer = new MatiposServer();
        matiposServer.requestMatiposServer(context, code, macAddress);
    }

    public void postValidationCode(Context context, String code, String macAddress, long faceId) {
        if (matiposServer == null) matiposServer = new MatiposServer();
        matiposServer.requestMatiposServer(context, code, macAddress, faceId);
    }

    public LiveData<MatiposResponseServer> getMatiposResponse() {
        if (matiposServer == null) {
            matiposServer = new MatiposServer();
        }

        return matiposServer.getMatiposResponseServer();
    }

    public void startProcessToEndingValidateCode(MediaPlayer mediaPlayer, int seconds) {
        new AsyncEvent().execute(mediaPlayer, seconds);
    }

    public LiveData<Boolean> IsProcessEnding() {
        if (processEnding == null) processEnding = new MutableLiveData<>();

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
                    if (processEnding == null) processEnding = new MutableLiveData<>();

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

    public LiveData<String> getQrValidationCode() {
        if (qrValidationCode == null) qrValidationCode = new MutableLiveData<>();
        return qrValidationCode;
    }

    public LiveData<String> getNfcValidationCode() {
        if (nfcValidationCode == null) nfcValidationCode = new MutableLiveData<>();
        return nfcValidationCode;
    }

    public boolean readersOk(Context context) {
        return ConfigUtil.isMatiposIsNfcReaderEnable(context) || ConfigUtil.isMatiposIsQrReaderEnable(context);
    }
}
