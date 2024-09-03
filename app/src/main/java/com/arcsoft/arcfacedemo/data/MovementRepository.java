package com.arcsoft.arcfacedemo.data;

import com.arcsoft.arcfacedemo.facedb.dao.MovementDao;
import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;

import java.util.List;

public class MovementRepository {
    private final MovementDao movementDao;
    private int currentIndex = 0;
    private final int pageSize;
    private static final String TAG = "MovementRepository";

    public MovementRepository(int pageSize, MovementDao movementDao) {
        this.pageSize = pageSize;
        this.movementDao = movementDao;
    }

    public List<MovementEntity> loadMore() {
        List<MovementEntity> movementEntities = movementDao.getMovements(currentIndex, pageSize);
        currentIndex += movementEntities.size();
        return movementEntities;
    }

    public List<MovementEntity> reload() {
        currentIndex = 0;
        return loadMore();
    }

    public int clearAll() {
        int movementCount = movementDao.deleteAll();
        currentIndex = 0;
        return movementCount;
    }

    public int delete(MovementEntity movementEntity) {
        return movementDao.deleteMovement(movementEntity);
    }

    public long insert(MovementEntity movementEntity) {
        return movementDao.insert(movementEntity);
    }

    public int update(MovementEntity movementEntity) {
        return movementDao.updateMovementEntity(movementEntity);
    }

    public int getTotalMovementCount() {
        return movementDao.getMovementCount();
    }
}
