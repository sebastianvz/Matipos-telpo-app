package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.data.MovementRepository;
import com.arcsoft.arcfacedemo.facedb.AppDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.MovementDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;
import com.arcsoft.arcfacedemo.matiposserver.MatiposServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditViewModel extends ViewModel {

    private MovementDao movementDao;
    private MovementRepository movementRepository;
    private static final int PAGE_SIZE = 7;

    private MutableLiveData<List<MovementEntity>> listMutableLiveData;

    public void init() {
        if (movementDao == null) {
            movementDao = AppDatabase.getInstance(ArcFaceApplication.getApplication()).movementDao();
        }

        if (movementRepository == null) {
            movementRepository = new MovementRepository(PAGE_SIZE, movementDao);
            new AsyncEvent().execute(0x01);
        }
    }

    public void loadMoreMovements() {
        new AsyncEvent().execute(0x01);
    }

    public void lastPage() {
        new AsyncEvent().execute(0x02);
    }

    public void purge() {
        new AsyncEvent().execute(0x03);
    }

    public LiveData<List<MovementEntity>> movements() {
        if (listMutableLiveData == null)
            listMutableLiveData = new MutableLiveData<>();
        return listMutableLiveData;
    }

    private class AsyncEvent {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        public void execute(int command) {

            executorService.execute(() -> {

                List<MovementEntity> movementEntityList = new ArrayList<>();
                switch (command) {
                    case 0x01:
                        movementEntityList = movementRepository.nextPage();
                        break;

                    case 0x02:
                        movementEntityList = movementRepository.lastPage();
                        break;

                    case 0x03:
                        movementRepository.clearAll();
                        break;
                }

                if (listMutableLiveData == null)
                    listMutableLiveData = new MutableLiveData<>();

                if (movementEntityList.size() > 0) {
                    for (int i = 0; i < movementEntityList.size(); i++) {
                        int faceId = movementEntityList.get(i).getFaceId();
                        if (faceId > 0) {
                            FaceEntity faceEntity = new MatiposServer().getFaceEntityByFaceId(faceId);
                            movementEntityList.get(i).setFaceEntity(faceEntity);
                        }
                    }
                }

                listMutableLiveData.postValue(movementEntityList);
            });
        }
    }
}
