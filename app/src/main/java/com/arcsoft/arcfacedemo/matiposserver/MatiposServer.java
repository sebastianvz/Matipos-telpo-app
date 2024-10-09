package com.arcsoft.arcfacedemo.matiposserver;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.data.MovementRepository;
import com.arcsoft.arcfacedemo.facedb.AppDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.MovementDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;
import com.arcsoft.arcfacedemo.ui.activity.MatiposServerSettingsActivity;
import com.arcsoft.arcfacedemo.util.ConfigUtil;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MatiposServer {
    static private MutableLiveData<MatiposResponseServer> matiposResponseServerMutableLiveData;

    public MatiposServer() {
    }

    public void run(Context context, String code, String macAddress) {
        MatiposRequestServer matiposRequestServer = new MatiposRequestServer(
                code,
                macAddress,
                ""
        );
        new AsyncMatiposRequest().run(context, matiposRequestServer);
    }

    public LiveData<MatiposResponseServer> getMatiposResponseServer() {
        if (matiposResponseServerMutableLiveData == null)
            matiposResponseServerMutableLiveData = new MutableLiveData<>();

        return matiposResponseServerMutableLiveData;
    }

    public void updateFaceIdByMovementById(Context context, int movementId) {
        FaceEntity faceEntity = AppDatabase.getInstance(context).faceDao().queryByLastFaceId();
        if (faceEntity != null) {
            MovementEntity movementEntity = AppDatabase.getInstance(context).movementDao().queryByMovementId(movementId);
            movementEntity.setFaceId((int) faceEntity.getFaceId());
            AppDatabase.getInstance(context).movementDao().updateMovementEntity(movementEntity);
        }
    }

    public FaceEntity getFaceEntityByFaceId(int faceId) {
        return AppDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao().queryByFaceId(faceId);
    }


    // Async task
    private static class AsyncMatiposRequest {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void run(Context context, MatiposRequestServer matiposRequestServer) {
            executorService.execute(() -> {
                MovementEntity movementEntity = new MovementEntity();
                movementEntity.operationType = "VALIDATE";
                movementEntity.requestData = matiposRequestServer.toString();
                movementEntity.requestDatetime = LocalDateTime.now().toString();

                // Request
                MatiposResponseServer matiposResponseServer = null;
                String errorMessage = "NULL";

                String matiposUrlServer = ConfigUtil.getMatiposUrlServer(context);
                if (matiposUrlServer != null) {
                    try {
                        matiposResponseServer = MatiposService.getInstance().sendPostRequest(matiposUrlServer, matiposRequestServer);
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                }

                movementEntity.responseData = matiposResponseServer == null ? errorMessage : matiposResponseServer.toString();
                movementEntity.responseDatetime = LocalDateTime.now().toString();

                long id = AppDatabase.getInstance(context).movementDao().insert(movementEntity);


                if (matiposResponseServer == null) {
                    matiposResponseServer = new MatiposResponseServer(null, null, null, errorMessage);
                }

                MatiposResponseServer response = matiposResponseServer;
                response.setIdMovement(id);

                if (matiposResponseServerMutableLiveData == null)
                    matiposResponseServerMutableLiveData = new MutableLiveData<>();

                matiposResponseServerMutableLiveData.postValue(response);

            });
        }
    }
}
