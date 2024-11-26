package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

interface WeatherRepository {
    fun addWeather(newWeatherNode: Weather): Weather
    fun getRecentWeather(): List<Weather>
    fun getAllWeather(): List<Weather>
    fun getWeatherAPI(): String
}