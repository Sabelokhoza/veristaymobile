package com.veristay.app;

import com.veristay.app.ApiService;
import com.veristay.app.SessionManager;
import com.veristay.app.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(SessionManager session) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                // Attach JWT token to every request
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = session.getToken();
                    if (token != null && !token.isEmpty()) {
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .header("Content-Type", "application/json")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                    return chain.proceed(original);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static RetrofitClient getInstance(SessionManager session) {
        if (instance == null) {
            instance = new RetrofitClient(session);
        }
        return instance;
    }

    public static void reset() {
        instance = null; // force re-init on login
    }

    public ApiService getApiService() {
        return apiService;
    }
}