package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

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

var recentWeatherLocations = mutableListOf(
    allWeatherLocations[0], allWeatherLocations[1]
)

class WeatherRepositoryImpl: WeatherRepository {
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