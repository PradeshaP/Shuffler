package com.example.shuffler.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpotifyApi {
    @GET("search")
    Call<SpotifySearchResponse> searchSongs(@Query("q") String query, @Query("type") String type);
}