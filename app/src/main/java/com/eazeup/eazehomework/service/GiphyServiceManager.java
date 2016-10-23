package com.eazeup.eazehomework.service;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GiphyServiceManager {
    private static final String TAG = GiphyServiceManager.class.getSimpleName();
    private static final String BASE_URL = "http://api.giphy.com/v1/gifs/";
    private static final String TOKEN = "dc6zaTOxFJmzC";

    private static final Map<String, String> QUERY_API_TOKEN;
    static {
        QUERY_API_TOKEN = new HashMap<>();
        QUERY_API_TOKEN.put("api_key", TOKEN);
    }

    private GiphyService mService;

    private static GiphyServiceManager sInstance = new GiphyServiceManager();

    public static GiphyServiceManager get() {
        return sInstance;
    }

    private GiphyServiceManager() {
        // Instantiate the Retrofit object for HTTP calls
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = retrofit.create(GiphyService.class);
    }

    public void getTrending(final Callback<GiphyResponse> callback) {
        Call<GiphyResponse> call = mService.getTrending(TOKEN);
        call.enqueue(callback);
    }
}
