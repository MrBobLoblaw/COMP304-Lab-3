package com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dylan.dylanmeszaros_comp304lab3_exercise1.RetrofitClient
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherObject
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WeatherViewModel(
    private var weatherRepository: WeatherRepository
): ViewModel() {
    /*private val _weatherData = MutableLiveData("No data");
    val weatherData: LiveData<String> get() = _weatherData;*/

    /*init{
        viewModelScope.launch {
            val testWeather = weatherRepository.getAllWeather()[0].latLng;
            getWeatherFromAPI(testWeather)
        }
    }*/

    suspend fun getWeatherFromAPI(location: LatLng): WeatherObject{
        val foundWeather = RetrofitClient.weatherAPI.getWeatherFromAPI(
            location.latitude, location.longitude, "metric", "b0b17820e92a987260edad235a8b01f1"
        )
        //_weatherData.value = foundWeather.name;
        return foundWeather;
        /*_weatherData.value = RetrofitClient.weather.getWeather(
            testWeather.latLng.latitude, testWeather.latLng.longitude, "metric", "b0b17820e92a987260edad235a8b01f1"
        ).toString();*/
    }

    fun addWeatherToFavorites(weatherObject: WeatherObject) = weatherRepository.addWeatherObject(weatherObject);
    fun getFavoriteWeatherObjects() = weatherRepository.getFavoriteWeatherObjects();
    fun getFavoriteIndex(weatherObject: WeatherObject) = weatherRepository.getFavoriteIndex(weatherObject);


    fun getRecentWeather() = weatherRepository.getRecentWeather();
    fun getAllWeather() = weatherRepository.getAllWeather();
    fun addWeather(newWeather: Weather) = weatherRepository.addWeather(newWeather);
    fun findRecentWeatherById(id: Int) = weatherRepository.findRecentWeatherById(id)
    fun getWeatherAPI() = weatherRepository.getWeatherAPI();
}