package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherRepository {
    fun addWeatherObject(weatherObject: WeatherObject): WeatherObject;
    fun getFavoriteWeatherObjects(): List<WeatherObject>;
    fun getFavoriteIndex(weatherObject: WeatherObject): Int?;

    fun addWeather(newWeatherNode: Weather): Weather;
    fun getRecentWeather(): List<Weather>;
    fun getAllWeather(): List<Weather>;
    fun findRecentWeatherById(id: Int): Weather;
    fun getWeatherAPI(): String;
}