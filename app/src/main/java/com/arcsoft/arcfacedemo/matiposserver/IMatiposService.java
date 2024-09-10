package com.arcsoft.arcfacedemo.matiposserver;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;

import java.net.SocketTimeoutException;

public interface IMatiposService {

    MatiposResponseServer sendPostRequest(String urlBase, MatiposRequestServer request) throws SocketTimeoutException;

}
