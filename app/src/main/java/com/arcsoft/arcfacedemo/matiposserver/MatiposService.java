package com.arcsoft.arcfacedemo.matiposserver;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MatiposService implements IMatiposService{
    private static MatiposService instance;

    public static MatiposService getInstance() {
        if (instance == null)
            instance = new MatiposService();
        return  instance;
    }

    @Override
    public MatiposResponseServer sendPostRequest(String urlBase, MatiposRequestServer request) throws SocketTimeoutException {
        HttpURLConnection httpCon = null;
        MatiposResponseServer matiposReponse;
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
                    matiposReponse = new MatiposResponseServer(
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
}
