package com.arcsoft.arcfacedemo.facedb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arcsoft.arcfacedemo.facedb.entity.MovementEntity;

import java.util.List;

@Dao
public interface MovementDao {
    @Query("select * from movement order by movementId desc limit :start, :size")
    List<MovementEntity> getMovements(int start, int size);

    @Update
    int updateMovementEntity(MovementEntity movementEntity);

    @Query("DELETE from movement")
    int deleteAll();

    @Delete
    int deleteMovement(MovementEntity movementEntity);

    @Insert
    long insert(MovementEntity movementEntity);

    @Query("select count(1) from movement")
    int getMovementCount();

    @Query("select * from movement where movementId = :movementId limit 1")
    MovementEntity queryByMovementId(int movementId);
}
