package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json

data class WeatherObject (
    @Json(name = "coord")
    var coord: LatLng,
    @Json(name = "weather")
    var weather: List<WeatherNode>,
    @Json(name = "base")
    var base: String,
    @Json(name = "main")
    var main: Main,
    @Json(name = "visibility")
    var visibility: Int,
    @Json(name = "wind")
    var wind: Wind,
    @Json(name = "timezone")
    var timezone: Int,
    @Json(name = "id")
    var id: Int,
    @Json(name = "name")
    var name: String,
)

data class WeatherNode (
    @Json(name = "main")
    var main: String,
    @Json(name = "description")
    var description: String,
)

data class Main(
    @Json(name = "temp")
    var temp: Double,
    @Json(name = "temp_min")
    var temp_min: Double,
    @Json(name = "temp_max")
    var temp_max: Double,
    @Json(name = "humidity")
    var humidity: Int,
)

data class Wind(
    @Json(name = "speed")
    var speed: Double,
)