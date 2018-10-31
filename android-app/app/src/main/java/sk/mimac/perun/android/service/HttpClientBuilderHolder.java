package sk.mimac.perun.android.service;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.HttpClientConnectionManager;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.socket.PlainConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.NoopHostnameVerifier;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.ssl.SSLContexts;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class HttpClientBuilderHolder {


    private static final HttpClientBuilder CLIENT_BUILDER;

    static {
        try {
            HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", new SSLConnectionSocketFactory(SSLContexts.custom()
                            .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true).build(), NoopHostnameVerifier.INSTANCE))
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()) // Only for testing
                    .build());
            CLIENT_BUILDER = HttpClientBuilder.create().setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(10_000)
                            .setConnectionRequestTimeout(10_000).build());
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException ex) {
            throw new RuntimeException("Can't initialize HttpClient", ex);
        }
    }

    public static HttpClient build() {
        return CLIENT_BUILDER.build();
    }

    /* PRIVATE METHODS */
    private HttpClientBuilderHolder() {
        // Utility class
    }

}
