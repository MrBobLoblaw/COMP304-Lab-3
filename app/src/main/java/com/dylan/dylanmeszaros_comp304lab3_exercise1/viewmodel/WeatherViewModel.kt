package com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel

import androidx.lifecycle.ViewModel
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepository

class WeatherViewModel(
    private var weatherRepository: WeatherRepository
): ViewModel() {

    fun getRecentWeather() = weatherRepository.getRecentWeather()
    fun getAllWeather() = weatherRepository.getAllWeather()
    fun addWeather(newWeather: Weather) = weatherRepository.addWeather(newWeather)
    fun getWeatherAPI() = weatherRepository.getWeatherAPI()
}