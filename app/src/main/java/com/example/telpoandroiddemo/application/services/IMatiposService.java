package com.example.telpoandroiddemo.application.services;

import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;

import java.net.SocketTimeoutException;

public interface IMatiposService {
    MatiposReponse sendPutRequest(String urlBase, MatiposRequest request) throws SocketTimeoutException;
    String getImageInBase64(String urlImage);
}
