package com.matibag.presentlast.api;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    // Replace with your actual Next.js API URL
    // For local testing:  "http://10.0.2.2:3000/" (Android emulator)
    // For test production Jezail: "http://192.168.100.13:3000/" (Local WiFi)
    // For production: "https://your-domain.com/"
    private static final String BASE_URL = "http://192.168.100.13:3000/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            Log.d(TAG, "Initializing Retrofit with BASE_URL: " + BASE_URL);

            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d("OkHttp", message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level. BODY);

            // Create OkHttpClient with logging and timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit. SECONDS)
                    .writeTimeout(30, TimeUnit. SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    . build();

            Log.d(TAG, "Retrofit initialized successfully");
        }
        return retrofit.create(ApiService.class);
    }
}