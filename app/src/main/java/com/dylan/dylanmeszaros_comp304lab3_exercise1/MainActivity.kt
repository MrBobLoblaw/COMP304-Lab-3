package com.dylan.dylanmeszaros_comp304lab3_exercise1

import android.Manifest
import com.dylan.dylanmeszaros_comp304lab3_exercise1.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.VolleyLog.TAG
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherObject
import com.dylan.dylanmeszaros_comp304lab3_exercise1.di.appModules
import com.dylan.dylanmeszaros_comp304lab3_exercise1.ui.theme.CoreTheme
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import com.dylan.dylanmeszaros_comp304lab3_exercise1.views.WeatherList
//import com.dylan.dylanmeszaros_comp304lab3_exercise1.views.getWeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import java.io.IOException


var onStartup = false;

lateinit var fusedLocationClient: FusedLocationProviderClient;
val defaultLocation = LatLng(43.6532, -79.3832); // Toronto as fallback

// Shared state for user location
var userLocationState = mutableStateOf(defaultLocation);

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onStartup == false){
            onStartup = true;
            startKoin {
                modules(appModules)
            }
        }
        enableEdgeToEdge()
        setContent {
            val weatherViewModel: WeatherViewModel = koinViewModel();
            CoreTheme {
                MainActivity_Main(onSwitch = {
                    startActivity(Intent(this@MainActivity, MapActivity::class.java));
                    finish();
                }, onExplore = { weatherObject ->
                    startActivity(Intent(this@MainActivity, ExploreActivity::class.java).apply{
                        putExtra("weatherID", weatherViewModel.getFavoriteIndex(weatherObject));
                    });
                    finish();
                }, this);
                //testWeatherData() // Test API
            }
        }
    }
}

@ExperimentalCoroutinesApi
class MapActivity : AppCompatActivity() {
    private lateinit var map: GoogleMap
    private lateinit var startAutocomplete: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Register the permission launcher
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation { location ->
                    // Update the location state from the result
                    userLocationState.value = location
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        };

        // Register PlacesAPI
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro")
        }

        //Log.i(TAG, "startAutocomplete started")

        // Searchbar activity
        startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Log.i(TAG, "Check for resultCode")
            if (result.resultCode == Activity.RESULT_OK) {
                //Log.i(TAG, "resultCode is ok")
                val intent = result.data
                if (intent != null) {
                    //Log.i(TAG, "intent exists")
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    //Log.i(TAG, "place recieved")
                    if (place.location != null) {
                        userLocationState.value = place.location!!;
                    }
                    else{
                        //Log.e(TAG, "Location is null");
                    }

                    //Log.i( TAG, "Place: ${place.displayName}, ${place.id}" )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                //Log.i(TAG, "User canceled autocomplete")
            }
        }
        //Log.i(TAG, "startAutocomplete initialized")

        setContent {

            var locationPermissionGranted by remember { mutableStateOf(false) };

            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(
                        this@MapActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location;
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }

            // Display Google Map with the current location
            MapActivity_Main(onSwitch = {
                startActivity(Intent(this@MapActivity, MainActivity::class.java));
                finish();
            }, locationPermissionGranted, onSearch = {
                //Log.i(TAG, "startAutocomplete initialized")
                // Set the fields to specify which types of place data to return after the user has made a selection.

                val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION)

                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this)
                startAutocomplete.launch(intent);
                //Log.i(TAG, "startAutocomplete finished")


            });
        }


    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume called")
    }

    // Fetch current location using FusedLocationProviderClient
    private fun fetchCurrentLocation(onLocationFetched: (LatLng) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(latLng)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                };
            }
        }
    }

}


class ExploreActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val weatherViewModel: WeatherViewModel = koinViewModel();
            val weatherID = intent.getIntExtra("weatherID", 0);

            CoreTheme {
                ExploreActivity_Main(onSwitch = {
                    startActivity(Intent(this@ExploreActivity, MainActivity::class.java));
                    finish();
                }, weatherViewModel.getFavoriteWeatherObjects()[weatherID], this);
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity_Main(onSwitch: () -> Unit, onExplore: (WeatherObject) -> Unit, context: Context) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Favorite Weather", fontWeight = FontWeight.Bold)
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSwitch) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Add more places");
            }
        },
        content =  { paddingValues ->
            WeatherList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                context = context,
                onExplore
            )
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapActivity_Main(onSwitch: () -> Unit, isLocationEnabled: Boolean, onSearch: () -> Unit) {
    val weatherViewModel: WeatherViewModel = koinViewModel();
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Weather Map", fontWeight = FontWeight.Bold)
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        content =  {

            // Google Maps Code Started

            MapScreen(isLocationEnabled);

            // Google Maps Code Ended

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top-left Floating Action Button
                FloatingActionButton(
                    onClick = { onSwitch() },
                    modifier = Modifier
                        .absolutePadding(
                            left = 10.dp,
                            top = 80.dp
                        )
                        .align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Go back");
                }
            }

            Box( // Background Box
                modifier = Modifier
                    .offset(
                        x = 70.dp,
                        y = 80.dp
                    )
                    .size(
                        width = 280.dp,
                        height = 55.dp
                    )
                    .background(Color.White, RoundedCornerShape(16.dp)),

            ) {
                Text(
                    modifier = Modifier
                        .offset(
                            x = 25.dp,
                            y = 15.dp,
                        )
                        .fillMaxSize(),
                    text = "Search for Location",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,  // Makes the text bold
                        fontSize = 22.sp             // Set the font size (adjust to your preference)
                    ),
                    textAlign = TextAlign.Center
                )
                FloatingActionButton(
                    onClick = { onSearch() },
                    modifier = Modifier
                        .offset( x = 25.dp, y = 15.dp, )
                        .fillMaxSize()
                        .alpha(0f),
                    containerColor = Color.Transparent
                ) { }
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top-left Floating Action Button
                FloatingActionButton(
                    onClick = {
                        onSearch();
                    },
                    modifier = Modifier
                        .absolutePadding(
                            left = 70.dp,
                            top = 80.dp
                        )
                        .align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search for Place");
                }
            }

            // Favorite Button
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top-left Floating Action Button
                FloatingActionButton(
                    onClick = {
                        val newWeatherObject = runBlocking {
                            weatherViewModel.getWeatherFromAPI(userLocationState.value);
                        }

                        weatherViewModel.addWeatherToFavorites(newWeatherObject);
                    },
                    modifier = Modifier
                        .absolutePadding(
                            left = 10.dp,
                            top = 140.dp
                        )
                        .align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Search for Place");
                }
            }

            /*var expanded by remember { mutableStateOf(false) };
            val items = weatherViewModel.getAllWeather();
            var selectedItem by remember { mutableStateOf(items[0]) };


            Box( // Background Box
                modifier = Modifier
                    .offset(
                        x = 70.dp,
                        y = 80.dp
                    )
                    .size(
                        width = 280.dp,
                        height = 55.dp
                    )
                    .background(Color.White, RoundedCornerShape(16.dp))
            ) { }
            Box( // Dropdown box
                modifier = Modifier.fillMaxSize()
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .absolutePadding(
                            top = 70.dp,
                            left = 70.dp
                        ),
                ) {
                    // TextField for displaying the selected item
                    OutlinedTextField(
                        value = selectedItem.placeName,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(),
                        label = { Text("Select an option") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    // Dropdown menu
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.placeName) },
                                onClick = {
                                    selectedItem = item;
                                    userLocationState.value = item.latLng;
                                    expanded = false;
                                    weatherViewModel.addWeather(item);
                                    //cameraNeedsUpdate = true;
                                }
                            )
                        }
                    }
                }
            }
            */
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(isLocationEnabled: Boolean) {

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocationState.value, 14f);
    };

    GoogleMap(
        modifier = Modifier
            .absolutePadding(
                top = 65.dp
            )
            .fillMaxWidth(1f)
            .fillMaxHeight(1f),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationEnabled,
            isIndoorEnabled = true
        )
    ) {

        // Add a marker at searched location
        Marker(
            state = MarkerState(position = userLocationState.value),
            title = "You are here",
            snippet = "Current Location"
        );

        // Zoom in on the marker
        cameraPositionState.move(update = CameraUpdateFactory.newLatLngZoom(userLocationState.value, 14f));

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreActivity_Main(onSwitch: () -> Unit, weatherObject: WeatherObject, context: Context) {

    var weatherViewModel: WeatherViewModel = koinViewModel()
    //var newWeather by remember { mutableStateOf(weather) };
    var isLoading by remember { mutableStateOf(true) };
    val uptodateWeatherObject = runBlocking {
        weatherViewModel.getWeatherFromAPI(LatLng(weatherObject.coord.latitude, weatherObject.coord.longitude));
    }

    // Fetch weather data and wait till done loading
    if (isLoading) {
        if (weatherObject != null){
            isLoading = false;
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = uptodateWeatherObject.name)
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        content =  { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {

            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .absolutePadding(
                left = 10.dp,
                top = 200.dp
            )
    ) {
        if (isLoading) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Spacer(modifier = Modifier.height(40.dp));
            Text(text = "Name: " + weatherObject.name, style = MaterialTheme.typography.headlineLarge);
            Spacer(modifier = Modifier.height(20.dp));
            Text(text = "Main: " + weatherObject.weather[0].main + ".", style = MaterialTheme.typography.headlineMedium);
            Spacer(modifier = Modifier.height(20.dp));
            Text(text = "Description: " + weatherObject.weather[0].description + ".", style = MaterialTheme.typography.headlineMedium);
            Spacer(modifier = Modifier.height(20.dp));
            Text(text = "Temperature: " + weatherObject.main.temp + " C", style = MaterialTheme.typography.headlineMedium);
            Spacer(modifier = Modifier.height(20.dp));
            Text(text = "Humidity: " + weatherObject.main.humidity, style = MaterialTheme.typography.headlineMedium);
            Spacer(modifier = Modifier.height(20.dp));
            Text(text = "Wind Speed: " + weatherObject.wind.speed + "m/s", style = MaterialTheme.typography.headlineMedium);
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top-left Floating Action Button
        FloatingActionButton(
            onClick = { onSwitch() },
            modifier = Modifier
                .absolutePadding(
                    left = 10.dp,
                    top = 100.dp
                )
                .align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Go back");
        }
    }
}
