package com.arcsoft.arcfacedemo.matiposserver;

import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

import java.net.SocketTimeoutException;
import java.util.List;

public interface IMatiposService {

    MatiposResponseServer sendPostRequest(String urlBase, MatiposRequestServer request) throws SocketTimeoutException;
    List<FaceEntity> getAllFaces(String urlBase) throws SocketTimeoutException;
    FaceEntity getByFaceId(String urlBase, int faceId) throws SocketTimeoutException;
    long insertFace(String urlBase, FaceEntity faceEntity, Bitmap imgBitmap) throws SocketTimeoutException;

    Bitmap downloadImageAsBitmap(String imageUrl);
}
