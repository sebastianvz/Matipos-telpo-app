package com.arcsoft.arcfacedemo.matiposserver;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

import java.net.SocketTimeoutException;
import java.util.List;

public interface IMatiposService {

    MatiposResponseServer sendPostRequest(String urlBase, MatiposRequestServer request) throws SocketTimeoutException;
    List<FaceEntity> getAllFaces(String urlBase) throws SocketTimeoutException;
}
