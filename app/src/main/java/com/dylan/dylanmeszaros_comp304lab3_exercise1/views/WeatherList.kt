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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherObject
import com.dylan.dylanmeszaros_comp304lab3_exercise1.userLocationState
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherList(modifier: Modifier, context: Context, onExplore: (WeatherObject) -> Unit) {
    var weatherViewModel: WeatherViewModel = koinViewModel()
    LazyColumn(
        modifier = modifier
    ) {
        items(weatherViewModel.getFavoriteWeatherObjects()) { weatherObject ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Display_WeatherCard(weatherObject, context, onExplore = { onExplore(weatherObject) });
            }
        }
    }
}

@Composable
fun Display_WeatherCard(weatherObject: WeatherObject, context: Context, onExplore: () -> Unit) {
    var weatherViewModel: WeatherViewModel = koinViewModel()
    //var newWeather by remember { mutableStateOf(weather) };
    var isLoading by remember { mutableStateOf(true) };
    val uptodateWeatherObject = runBlocking {
        weatherViewModel.getWeatherFromAPI(LatLng(weatherObject.coord.latitude, weatherObject.coord.longitude));
    }
    //weatherObject = updatedWeatherObject;

    //weatherObject.id = uptodateWeatherObject.id;

    // Fetch weather data and wait till done loading
    if (isLoading) {
        if (weatherObject != null){
            isLoading = false;
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = weatherObject.name, style = MaterialTheme.typography.headlineLarge);
                    IconButton(onClick = onExplore) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Explore"
                        );
                    }
                }
                Spacer(modifier = Modifier.height(4.dp));
                Text(text = weatherObject.weather[0].description.take(50) + "...", style = MaterialTheme.typography.bodyMedium);
                Spacer(modifier = Modifier.height(4.dp));
                Text(text = weatherObject.main.temp.toString() + " C", style = MaterialTheme.typography.bodyMedium);
            }
        }
    }
}

/*fun getWeatherData(weather: Weather, context: Context, onResult: (Weather) -> Unit) {
    val queue = Volley.newRequestQueue(context);
    val url = "https://api.openweathermap.org/data/2.5/weather?lat=${weather.latLng.latitude}&lon=${weather.latLng.longitude}&units=metric&appid=b0b17820e92a987260edad235a8b01f1";

    val stringReq = StringRequest(
        Request.Method.GET, url, { response ->
            val obj = JSONObject(response);

            // Extract main object
            val main = obj.getJSONObject("main");
            val temperature = main.getString("temp");

            // Extract name object
            val city = obj.getString("name");

            // Extract weather object
            val weatherNode = obj.getJSONArray("weather").getJSONObject(0);
            val description = weatherNode.getString("description");

            // Return the updated weather object
            val newWeather = weather.copy(
                placeName = city,
                temperature = temperature,
                description = description
            );
            onResult(newWeather);
        },
        { error ->
            Log.e("getWeatherData", "Error: ${error.message}");
            onResult(weather);
        }
    )
    queue.add(stringReq);
}*/

public fun updateWeatherObject(current: WeatherObject, updated: WeatherObject){

}