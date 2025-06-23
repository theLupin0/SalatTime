package com.guven.salattime;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("appid") String key,
            @Query("units") String units
    );
}
