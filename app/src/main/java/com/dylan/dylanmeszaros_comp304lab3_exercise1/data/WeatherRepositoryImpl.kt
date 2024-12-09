package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

import androidx.compose.foundation.lazy.items
import com.google.android.gms.maps.model.LatLng

var allWeatherLocations = mutableListOf(
    Weather(1, "Hamilton", "A dusty city", LatLng(43.25619, -79.86724),
        "0"),
    Weather(2, "Toronto", "A busy city", LatLng(43.65556, -79.37395),
        "0"),
    Weather(3, "New York", "Hate this city", LatLng(40.71663, -74.00166),
        "0"),
    Weather(4, "Burlington", "no clue where this is", LatLng(43.32603,-79.78877),
        "0"),
    Weather(5, "Guelph", "Home sweet home", LatLng(43.54411,-80.24704),
        "0"),
    Weather(6, "Kitchener", "Has a shop named tricks galore", LatLng(43.44960,-80.49074),
        "0")

)

var favoriteWeatherObjects: MutableList<WeatherObject> = mutableListOf();

var recentWeatherLocations = mutableListOf(
    allWeatherLocations[0], allWeatherLocations[1]
)

class WeatherRepositoryImpl: WeatherRepository {
    /*override suspend fun getWeather(lat: Double, lng: Double, units: String, id: String): Weather {
        return allWeatherLocations[1];
    }*/

    override fun addWeatherObject(weatherObject: WeatherObject): WeatherObject {
        favoriteWeatherObjects.add(0, weatherObject);
        cleanDuplicatePlaces();
        return weatherObject;
    }
    override fun getFavoriteWeatherObjects(): List<WeatherObject> {
        return favoriteWeatherObjects
    }

    override fun getFavoriteIndex(weatherObject: WeatherObject): Int? {
        for (i in 0..favoriteWeatherObjects.count()){
            if (favoriteWeatherObjects[i].id == weatherObject.id){
                return i;
            }
        }
        return null;
    }


    override fun addWeather(newWeatherNode: Weather): Weather {
        recentWeatherLocations.add(0, newWeatherNode);
        cleanDuplicates();
        return newWeatherNode;
    }
    override fun getRecentWeather(): List<Weather> {
        return recentWeatherLocations;
    }
    override fun getAllWeather(): List<Weather>{
        return allWeatherLocations;
    }

    override fun findRecentWeatherById(id: Int): Weather {
        return allWeatherLocations.find { it.id == id }
            ?: throw NoSuchElementException("Weather not found: " + id)
    }
    override fun getWeatherAPI(): String {
        return "@string/weather_api_key"; // API Key
    }
}

fun cleanDuplicates() {
    val seenIds = mutableSetOf<Int>();
    recentWeatherLocations = recentWeatherLocations.filter { weather ->
        val isNew = seenIds.add(weather.id);
        isNew;
    }.toMutableList();
}

fun cleanDuplicatePlaces() {
    val seenIds = mutableSetOf<Int>();
    favoriteWeatherObjects = favoriteWeatherObjects.filter { weatherObject ->
        val isNew = seenIds.add(weatherObject.id);
        isNew;
    }.toMutableList();
}