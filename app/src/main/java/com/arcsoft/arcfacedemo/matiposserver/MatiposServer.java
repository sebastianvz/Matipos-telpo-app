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
    static private String urlBase = null;

    public MatiposServer() {
    }

    public void requestMatiposServer(Context context, String code, String macAddress) {
        if (urlBase == null) {
            urlBase = ConfigUtil.getUrlBaseFaceRepository(context);
        }
        MatiposRequestServer matiposRequestServer = new MatiposRequestServer(
                code,
                macAddress,
                ""
        );
        new AsyncMatiposRequest().run(context, matiposRequestServer, 0);
    }

    public void requestMatiposServer(Context context, String code, String macAddress, long faceId) {
        if (urlBase == null) {
            urlBase = ConfigUtil.getUrlBaseFaceRepository(context);
        }
        MatiposRequestServer matiposRequestServer = new MatiposRequestServer(
                code,
                macAddress,
                ""
        );
        matiposRequestServer.setFaceId(faceId);
        new AsyncMatiposRequest().run(context, matiposRequestServer, faceId);
    }

    public void insertLogMovement(Context context, MovementEntity movementEntity) {
        new AsyncInsertMovement().run(context, movementEntity);
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

    /********* New Methods **********/
    public void GetAllFaces()
    {
        new AsyncGetAllFaces().run();
    }


    // Async task
    private static class AsyncMatiposRequest {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        public void run(Context context, MatiposRequestServer matiposRequestServer, long faceId) {
            executorService.execute(() -> {
                MovementEntity movementEntity = new MovementEntity();
                movementEntity.operationType = faceId == -1 ? "EXIT" : "VALIDATE";
                movementEntity.requestData = matiposRequestServer.toString();
                movementEntity.requestDatetime = LocalDateTime.now().toString();

                if (faceId > 0) {

                    movementEntity.faceId = (int) faceId;
                    movementEntity.operationType = "EXIT";

                    MatiposResponseServer response = null;
                    MovementEntity lastMovement = AppDatabase.getInstance(context).movementDao().queryGetLastByFaceId((int) faceId);
                    if (lastMovement != null) {
                        if (!lastMovement.parseRequest().getEntryCode().equals(matiposRequestServer.getEntryCode())) {
                            response = new MatiposResponseServer(Boolean.FALSE, matiposRequestServer.getAddress(), LocalDateTime.now().toString(), "Codigo no coincide");
                        }
                    }
                    else {
                        response = new MatiposResponseServer(Boolean.FALSE, matiposRequestServer.getAddress(), LocalDateTime.now().toString(), "No existe registro con codigo");
                    }

                    if (response != null) {
                        movementEntity.responseData = response.toString();
                        movementEntity.responseDatetime = LocalDateTime.now().toString();

                        AppDatabase.getInstance(context).movementDao().insert(movementEntity);

                        if (matiposResponseServerMutableLiveData == null)
                            matiposResponseServerMutableLiveData = new MutableLiveData<>();

                        matiposResponseServerMutableLiveData.postValue(response);

                        return;
                    }

                }

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

    private static class AsyncInsertMovement {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        public void run(Context context, MovementEntity movementEntity) {
            executorService.execute(() -> {
                long id = AppDatabase.getInstance(context).movementDao().insert(movementEntity);
            });
        }
    }

    /*********** New Methods **********/
    private static class AsyncGetAllFaces {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        public void run() {
            executorService.execute(() -> {
                try {
                    MatiposService.getInstance().getAllFaces(urlBase);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class AsyncSendFaceData {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        public void run() {
            executorService.execute(() -> {
                try {
                    MatiposService.getInstance().getAllFaces(urlBase);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
