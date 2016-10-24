package com.eazeup.eazehomework.service;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GiphyService {

    @GET("trending")
    Call<GiphyResponse> getTrending(@Query("offset") int offset, @Query("limit") int limit,
                                    @Query("api_key") String apiKey);

    @GET("search")
    Call<GiphyResponse> getSearch(@Query("q") String searchQuery, @Query("offset") int offset,
                                  @Query("limit") int limit, @Query("api_key") String apiKey);

    @GET("{gifId}")
    Call<GiphyDetailResponse> getById(@Path("gifId") String gifId, @Query("api_key") String apiKey);
}
