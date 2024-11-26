package com.dylan.dylanmeszaros_comp304lab3_exercise1.views

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.VolleyLog.TAG
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.userLocationState
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherList(modifier: Modifier, context: Context) {
    var weatherViewModel: WeatherViewModel = koinViewModel()
    LazyColumn(
        modifier = modifier
    ) {
        items(weatherViewModel.getRecentWeather()) { weather ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //Text(text = "Name: ${task.name}")
                //Text(text = "Species: ${task.status}")
                Display_WeatherCard(weather, context);
            }
        }
    }
}

@Composable
fun Display_WeatherCard(weather: Weather, context: Context) {

    var newWeather by remember { mutableStateOf(weather) }
    var isLoading by remember { mutableStateOf(true) }

    newWeather = getWeatherData(weather, context)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = newWeather.placeName, style = MaterialTheme.typography.headlineLarge);
            }
            Spacer(modifier = Modifier.height(4.dp));
            Text(text = newWeather.description.take(50) + "...", style = MaterialTheme.typography.bodyMedium); // Truncate content
            Spacer(modifier = Modifier.height(4.dp));
            Text(text = newWeather.temperature + " C", style = MaterialTheme.typography.bodyMedium); // Truncate content
        }
    }
}

private fun getWeatherData(weather: Weather, context: Context): Weather {
    // Instantiate the RequestQueue.
    val queue = Volley.newRequestQueue(context)
    val url: String = "https://api.openweathermap.org/data/2.5/weather?lat=${weather.latLng.latitude}&lon=${weather.latLng.longitude}&units=metric&appid=b0b17820e92a987260edad235a8b01f1"

    var newWeather = weather;

    // Request a string response
    // from the provided URL.
    val stringReq = StringRequest(
        Request.Method.GET, url, { response ->
        // get the JSON object
        val obj = JSONObject(response)

        // Getting the temperature readings from response
        val main: JSONObject = obj.getJSONObject("main")
        val temperature = main.getString("temp")
            newWeather.temperature = temperature
        println(temperature)

        // Getting the city name
        val city = obj.getString("name")
        println(city)

        // set the temperature and the city
        // name using getString() function
        //textView.text = "${temperature} deg Celcius in ${city}"
    },
        // In case of any error
        { Log.e(TAG, "Place:") }
    )
    queue.add(stringReq)

    return newWeather;
}