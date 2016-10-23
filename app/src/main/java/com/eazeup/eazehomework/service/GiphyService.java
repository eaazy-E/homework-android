package com.eazeup.eazehomework.service;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GiphyService {

    @GET("trending")
    Call<GiphyResponse> getTrending(@Query("api_key") String apiKey);
}
