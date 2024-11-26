package com.dylan.dylanmeszaros_comp304lab3_exercise1

import android.Manifest
import com.dylan.dylanmeszaros_comp304lab3_exercise1.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.di.appModules
import com.dylan.dylanmeszaros_comp304lab3_exercise1.ui.theme.CoreTheme
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import com.dylan.dylanmeszaros_comp304lab3_exercise1.views.WeatherList
import com.dylan.dylanmeszaros_comp304lab3_exercise1.views.getWeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import java.io.IOException


var onStartup = false;

lateinit var fusedLocationClient: FusedLocationProviderClient;
val defaultLocation = LatLng(43.6532, -79.3832); // Toronto as fallback

// Shared state for user location
var userLocationState = mutableStateOf(defaultLocation);
var cameraNeedsUpdate = false;

class MainActivity : ComponentActivity() {
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
            CoreTheme {
                MainActivity_Main(onSwitch = {
                    startActivity(Intent(this@MainActivity, MapActivity::class.java));
                    finish();
                }, onExplore = { weather ->
                    startActivity(Intent(this@MainActivity, ExploreActivity::class.java).apply{
                        putExtra("weatherID", weather.id);
                    });
                    finish();
                }, this);
            }
        }
    }
}

@ExperimentalCoroutinesApi
class MapActivity : AppCompatActivity() {
    private lateinit var map: GoogleMap

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
            }, locationPermissionGranted);
        }


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
                }, weatherViewModel.getAllWeather()[weatherID], this);
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity_Main(onSwitch: () -> Unit, onExplore: (Weather) -> Unit, context: Context) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Recent Weather")
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSwitch) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Add more weather nodes");
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
fun MapActivity_Main(onSwitch: () -> Unit, isLocationEnabled: Boolean) {
    val weatherViewModel: WeatherViewModel = koinViewModel();
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Weather Map")
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

            var expanded by remember { mutableStateOf(false) };
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

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(isLocationEnabled: Boolean) {

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocationState.value, 14f);
    };

    // Track when the camera needs an update
    var cameraNeedsUpdate by remember { mutableStateOf(cameraNeedsUpdate) };

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
fun ExploreActivity_Main(onSwitch: () -> Unit, weather: Weather, context: Context) {

    var weatherViewModel: WeatherViewModel = koinViewModel()
    var newWeather by remember { mutableStateOf(weather) };
    var isLoading by remember { mutableStateOf(true) };

    // Fetch weather data and wait till done loading
    if (isLoading) {
        getWeatherData(weather, context) { fetchedWeather ->
            newWeather = fetchedWeather;
            isLoading = false;
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = weather.placeName)
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
            Text(text = "Name: " + weather.placeName, style = MaterialTheme.typography.headlineLarge);
            Spacer(modifier = Modifier.height(40.dp));
            Text(text = "Description: " + weather.description + ".", style = MaterialTheme.typography.headlineMedium);
            Spacer(modifier = Modifier.height(40.dp));
            Text(text = "Temperature: " + weather.temperature + " C", style = MaterialTheme.typography.headlineMedium);
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
