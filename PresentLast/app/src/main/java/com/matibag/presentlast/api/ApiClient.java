package com.matibag.presentlast.api;

import android.util.Log;
import com.matibag.presentlast.BuildConfig; // Import the generated config
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";

    // Use the value from build.gradle.kts automatically based on build variant
    private static final String BASE_URL = BuildConfig.API_BASE_URL;

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            Log.d(TAG, "Initializing Retrofit with BASE_URL: " + BASE_URL);

            // Create logging interceptor with truncation for long JSON responses
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                if (message.length() > 4000) {
                    Log.d("OkHttp", message.substring(0, 4000) + "... [truncated]");
                } else {
                    Log.d("OkHttp", message);
                }
            });

            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configure OkHttpClient with common headers and timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json")
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            // Build Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
            Log.d(TAG, "Retrofit initialized successfully");
        }
        return apiService;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void resetClient() {
        retrofit = null;
        apiService = null;
        Log.d(TAG, "ApiClient reset");
    }

    public static boolean isProduction() {
        return BASE_URL.contains("workers.dev") || BASE_URL.startsWith("https://");
    }
}