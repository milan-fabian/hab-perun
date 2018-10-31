package sk.mimac.perun.android.service;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.util.EntityUtils;

import java.io.IOException;
import java.net.SocketException;

import sk.mimac.perun.model.PayloadStatus;

public class ServiceConnector {

    private static final String TAG = "ServiceConnector";

    private static final int HTTP_OK = 200;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static String credentials;
    private static String url;

    public static void setCredentials(String credentials) {
        ServiceConnector.credentials = credentials;
    }

    public static void setUrl(String url) {
        ServiceConnector.url = url;
    }

    public static void sendSensorData(PayloadStatus payloadStatus) {
        try {
            HttpPost post = new HttpPost(url + "/status");
            String data = OBJECT_MAPPER.writeValueAsString(payloadStatus);
            StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);
            execute(HttpClientBuilderHolder.build(), post);
        } catch (SocketException | BadResponseCodeException ex) {
            Log.w(TAG, "Can't send sensor data to server: " + ex.toString());
        } catch (Exception ex) {
            Log.w(TAG,"Can't send sensor data to server", ex);
        }
    }

    public static void sendImage(byte[] imageData) {
        try {
            HttpPost post = new HttpPost(url + "/image");
            ByteArrayEntity requestEntity = new ByteArrayEntity(imageData);
            post.setEntity(requestEntity);
            execute(HttpClientBuilderHolder.build(), post);
        } catch (SocketException | BadResponseCodeException ex) {
            Log.w(TAG, "Can't send image to server: " + ex.toString());
        } catch (Exception ex) {
            Log.w(TAG,"Can't send image to server", ex);
        }
    }

    private static HttpResponse execute(HttpClient client, HttpRequestBase reqest) throws IOException {
        reqest.setHeader("Authorization", "Basic " + credentials);
        HttpResponse response = client.execute(reqest);
        checkResultCode(response);
        return response;
    }

    private static void checkResultCode(HttpResponse response) throws IOException {
        int code = response.getStatusLine().getStatusCode();
        if (code != HTTP_OK) {
            EntityUtils.consumeQuietly(response.getEntity());
            throw new BadResponseCodeException("Got status code " + code + " from server");
        }
    }

    private static class BadResponseCodeException extends IOException {

        public BadResponseCodeException(String message) {
            super(message);
        }

    }

}
