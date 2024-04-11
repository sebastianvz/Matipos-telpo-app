package com.example.telpoandroiddemo.infraestructure.services;

import com.example.telpoandroiddemo.application.services.IMatiposService;
import com.example.telpoandroiddemo.domain.models.MatiposReponse;
import com.example.telpoandroiddemo.domain.models.MatiposRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MatiposService implements IMatiposService {

    private static MatiposService instance;

    public static MatiposService getInstance() {
        if (instance == null)
            instance = new MatiposService();
        return  instance;
    }

    @Override
    public MatiposReponse sendPostRequest(String urlBase, MatiposRequest request) throws SocketTimeoutException {
        HttpURLConnection httpCon = null;
        MatiposReponse matiposReponse;
        try {
            URL url = new URL(urlBase);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
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
                    InputStream inputStream = httpCon.getInputStream();
                    int readLen;
                    final int bufLen = 1024;
                    byte[] buf = new byte[bufLen];
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                        outputStream.write(buf, 0, readLen);
                    String response = outputStream.toString();
                    JSONObject jsonObject = new JSONObject(response);
                    matiposReponse = new MatiposReponse(
                            (Boolean) jsonObject.get("state"),
                            (String) jsonObject.get("direction"),
                            (String) jsonObject.get("date"),
                            (String) jsonObject.get("answer")
                    );
                    break;

                case HttpURLConnection.HTTP_CREATED:
                    throw new RuntimeException("Request successful. New resource created.");

                case HttpURLConnection.HTTP_BAD_REQUEST:
                    InputStream errorStream = httpCon.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                    String line;
                    StringBuilder responseBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    String errorResponse = responseBuilder.toString();
                    JSONObject jsonData = new JSONObject(errorResponse);

                    String errorMessage = (String) jsonData.get("message");
                    throw new RuntimeException(errorMessage);

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new RuntimeException("Unauthorized!");

                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw new RuntimeException("Forbidden!");

                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new RuntimeException("Resource not found!");

                default:
                    throw new RuntimeException("Response Code : " + responseCode);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            if (e instanceof java.net.SocketTimeoutException) {
                throw new RuntimeException("Connection timed out");
            }
            else {
                throw new RuntimeException(e.getMessage());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage());
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

            InputStream inputStream = connection.getInputStream();
            int readLen;
            final int bufLen = 1024*8;
            byte[] buf = new byte[bufLen];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                outputStream.write(buf, 0, readLen);
            return outputStream.toString();
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
