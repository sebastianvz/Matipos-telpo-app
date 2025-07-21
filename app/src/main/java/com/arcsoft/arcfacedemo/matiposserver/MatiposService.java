package com.arcsoft.arcfacedemo.matiposserver;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.arcsoft.arcfacedemo.common.MatiposRequestServer;
import com.arcsoft.arcfacedemo.common.MatiposResponseServer;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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


    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string debe tener longitud par");
        }

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }

        return data;
    }

    @Override
    public List<FaceEntity> getAllFaces(String urlBase) throws SocketTimeoutException {
        HttpURLConnection httpCon = null;
        List<FaceEntity> faceEntityList = new ArrayList<>();
        try {
            URL url = new URL(urlBase);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);

            int responseCode = httpCon.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpCon.getInputStream())
                );

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parsear el array JSON
                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    // Create List de faceEntity
                    FaceEntity faceEntity = new FaceEntity(
                            obj.getString("user_name"),
                            obj.getString("image_path"),
                            hexStringToByteArray(obj.getString("feature_data"))
                    );
                    faceEntity.setFaceId(obj.getInt("faceId"));

                    faceEntityList.add(i, faceEntity);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) httpCon.disconnect();
        }
        return faceEntityList;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public FaceEntity getByFaceId(String urlBase, int faceId) throws SocketTimeoutException {
        HttpURLConnection httpCon = null;
        FaceEntity faceEntity = null;
        try {
            urlBase = String.format("%s/%d", urlBase, faceId);
            URL url = new URL(urlBase);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);

            int responseCode = httpCon.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpCon.getInputStream())
                );

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parsear el array JSON
                JSONObject jsonObject = new JSONObject(response.toString());
                // Create List de faceEntity
                faceEntity = new FaceEntity(
                        jsonObject.getString("user_name"),
                        jsonObject.getString("image_path"),
                        hexStringToByteArray(jsonObject.getString("feature_data"))
                );
                faceEntity.setFaceId(jsonObject.getInt("faceId"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) httpCon.disconnect();
        }
        return faceEntity;
    }

    private byte[] parseImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    @Override
    public void insertFace(String urlBase, FaceEntity faceEntity, Bitmap imgBitmap) {
        HttpURLConnection httpCon = null;
        try {
            // Convertir imagen a Base64
            byte[] imageBytes = parseImage(imgBitmap);
            String imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // Crear JSON a enviar
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_name", faceEntity.getUserName());
            jsonBody.put("image_path", faceEntity.getImagePath());
            jsonBody.put("feature_data", bytesToHex(faceEntity.getFeatureData()));
            jsonBody.put("register_time", faceEntity.getRegisterTime());
            jsonBody.put("image", imageBase64);

            // Abrir conexión
            URL url = new URL(urlBase);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpCon.setDoOutput(true);
            httpCon.setConnectTimeout(5000);

            // Escribir JSON en el body
            OutputStream os = httpCon.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(jsonBody.toString());
            writer.flush();
            writer.close();
            os.close();

            // Leer respuesta
            int responseCode = httpCon.getResponseCode();
            InputStream inputStream;

            // Si es éxito (200, 201), usamos getInputStream(), si no, getErrorStream()
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                inputStream = httpCon.getInputStream();
            } else {
                inputStream = httpCon.getErrorStream(); // para errores como 422
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Mostrar la respuesta completa
            Log.d("HTTP_RESPONSE", "Code: " + responseCode + " Body: " + response.toString());


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) httpCon.disconnect();
        }
    }

    public Bitmap downloadImageAsBitmap(String imageUrl) {
        HttpURLConnection httpCon = null;
        try {
            URL url = new URL(imageUrl);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            httpCon.connect();

            int responseCode = httpCon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpCon.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            } else {
                Log.e("DownloadImage", "Error HTTP: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }
        return null;
    }

}
