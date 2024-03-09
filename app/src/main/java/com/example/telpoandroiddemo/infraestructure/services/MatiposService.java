package com.example.telpoandroiddemo.infraestructure.services;

import com.example.telpoandroiddemo.application.services.IMatiposService;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Base64;

public class MatiposService implements IMatiposService {

    private static MatiposService instance;
    private final String targetUrl;

    public static MatiposService getInstance(String targetUrl) {
        if (instance == null)
            instance = new MatiposService(targetUrl);
        return  instance;
    }

    public MatiposService(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public MatiposReponse sendPutRequest(String urlBase, MatiposRequest request) throws SocketTimeoutException {
        HttpURLConnection httpCon = null;
        MatiposReponse matiposReponse = null;
        try {
            URL url = new URL(urlBase);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            httpCon.setRequestProperty("Content-Type", "application/json");

            // Set the connection timeout to 5000 milliseconds
            httpCon.setConnectTimeout(5000);

            // Set the read timeout to 5000 milliseconds
            httpCon.setReadTimeout(5000);

            OutputStream out = httpCon.getOutputStream();
            String requestBody = request.toString();
            out.write(requestBody.getBytes());
            out.flush();
            out.close();

            int responseCode = httpCon.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    String response = new String(httpCon.getInputStream().readAllBytes());
                    JSONObject jsonObject = new JSONObject(response);
                        matiposReponse  = new MatiposReponse(
                                (Boolean) jsonObject.get("status"),
                                (String) jsonObject.get("address"),
                                (String) jsonObject.get("date"),
                                (String) jsonObject.get("answer")
                        );
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    // TODO
                    System.out.println("Request successful. New resource created.");
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    // TODO
                    System.out.println("Bad Request!");
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    // TODO
                    System.out.println("Unauthorized!");
                    break;
                case HttpURLConnection.HTTP_FORBIDDEN:
                    // TODO
                    System.out.println("Forbidden!");
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // TODO
                    System.out.println("Resource not found!");
                    break;
                default:
                    // TODO
                    System.out.println("Response Code : " + responseCode);
                    break;
            }

        } catch (MalformedURLException e) {
            // TODO
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            // TODO
            System.out.println("IO Exception: " + e.getMessage());
            // raise timeout exception
            if (e instanceof java.net.SocketTimeoutException) {
                throw new java.net.SocketTimeoutException("Connection timed out");
            }
        } catch (JSONException e) {
            // TODO
            System.out.println("JSONException: " + e.getMessage());
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }
        return matiposReponse;
    }

    @Override
    public String getImageInBase64(String urlImage) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlImage);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            // Set the connection timeout to 5000 milliseconds
            connection.setConnectTimeout(5000);

            // Set the read timeout to 5000 milliseconds
            connection.setReadTimeout(5000);
            connection.connect();
            return new String(connection.getInputStream().readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
