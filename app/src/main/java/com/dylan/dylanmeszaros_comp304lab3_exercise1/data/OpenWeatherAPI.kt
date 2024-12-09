package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherAPI {
    @GET("weather")
    suspend fun getWeatherFromAPI(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherObject;
}