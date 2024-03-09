package com.example.telpoandroiddemo.application.services;

import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface MatiposService {

    @GET("/GetCodeStatus")
    Call<MatiposReponse> get();

}
