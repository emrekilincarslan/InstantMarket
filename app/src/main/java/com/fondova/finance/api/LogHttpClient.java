package com.fondova.finance.api;

import com.fondova.finance.FlavorConstants;
import com.fondova.finance.api.socket.GzipRequestInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class LogHttpClient {

    private static final long TIMEOUT_SECONDS = 60 * 1000;

    private final OkHttpClient okHttpClient;
    private final OkHttpClient okHttpClientWithoutGzip;

    @Inject
    public LogHttpClient() {
        okHttpClient = createClient(true);
        okHttpClientWithoutGzip = createClient(false);
    }

    private OkHttpClient createClient(Boolean withGzip) {
        OkHttpClient okHttpClient;
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (FlavorConstants.SHOULD_LOG_RESTFUL_COMMUNICATION) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            if (withGzip) {
                builder.addInterceptor(new GzipRequestInterceptor());
            }
            builder.addInterceptor(interceptor);
        }
        okHttpClient = builder.build();
        return okHttpClient;
    }

    public OkHttpClient get() {
        return okHttpClient;
    }

    public OkHttpClient withoutGzip() {
        return okHttpClientWithoutGzip;
    }
}

