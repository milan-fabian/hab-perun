package sk.mimac.perun.android.webclient;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.util.EntityUtils;
import sk.mimac.perun.model.PayloadStatus;

public class ServiceConnector {

    private static final String TAG = "ServiceConnector";

    private static final int HTTP_OK = 200;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static String credentials;
    private static List<String> urls = Collections.emptyList();

    public static void setCredentials(String credentials) {
        ServiceConnector.credentials = credentials;
    }

    public static void setUrls(List<String> urls) {
        ServiceConnector.urls = urls;
    }

    public static void sendSensorData(PayloadStatus payloadStatus) {
        for (String url : urls) {
            try {
                HttpPost post = new HttpPost(url + "/payload/status");
                String data = OBJECT_MAPPER.writeValueAsString(payloadStatus);
                writeToFile(data);
                StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
                post.setEntity(requestEntity);
                execute(HttpClientBuilderHolder.build(), post);
            } catch (SocketException | BadResponseCodeException ex) {
                Log.w(TAG, "Can't send sensor data to server: " + ex.toString());
            } catch (Exception ex) {
                Log.w(TAG, "Can't send sensor data to server", ex);
            }
        }
    }

    public static void sendImage(String camera, byte[] imageData) {
        for (String url : urls) {
            try {
                HttpPost post = new HttpPost(url + "/payload/image?camera=" + camera);
                ByteArrayEntity requestEntity = new ByteArrayEntity(imageData);
                post.setEntity(requestEntity);
                execute(HttpClientBuilderHolder.build(), post);
                return; // Image is send only to one server, in order to preserve data usage
            } catch (SocketException | BadResponseCodeException ex) {
                Log.w(TAG, "Can't send image to server: " + ex.toString());
            } catch (Exception ex) {
                Log.w(TAG, "Can't send image to server", ex);
            }
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

    private static void writeToFile(String data) {
        File file = new File(Environment.getExternalStorageDirectory(), "hab-perun/data.txt");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
            writer.write(data);
            writer.write('\n');
        } catch (IOException ex) {
            Log.w(TAG, "Can't write data to file", ex);
        }
    }

    private static class BadResponseCodeException extends IOException {

        public BadResponseCodeException(String message) {
            super(message);
        }

    }

}
