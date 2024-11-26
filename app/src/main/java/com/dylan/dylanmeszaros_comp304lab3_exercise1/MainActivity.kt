package com.dylan.dylanmeszaros_comp304lab3_exercise1

import android.Manifest
//import android.R
import com.dylan.dylanmeszaros_comp304lab3_exercise1.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.dylan.dylanmeszaros_comp304lab3_exercise1.di.appModules
import com.dylan.dylanmeszaros_comp304lab3_exercise1.ui.theme.CoreTheme
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import com.dylan.dylanmeszaros_comp304lab3_exercise1.views.WeatherList
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.kotlin.PlaceSelectionError
import com.google.android.libraries.places.widget.kotlin.PlaceSelectionSuccess
import com.google.android.libraries.places.widget.kotlin.placeSelectionEvents
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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

lateinit var fusedLocationClient: FusedLocationProviderClient
val defaultLocation = LatLng(43.6532, -79.3832) // Toronto as fallback

// Shared state for user location
var userLocationState = mutableStateOf(defaultLocation)
var cameraNeedsUpdate = false;

// Weather
var weatherURL = "https://api.openweathermap.org/data/2.5/weather?lat=${userLocationState.value.latitude}&lon=${userLocationState.value.longitude}&units=metric&appid=@string/weather_api_key"

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
        setContentView(R.layout.activity_main)

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
        }

        setContent {

            var locationPermissionGranted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(
                        this@MapActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            // Display Google Map with the current location
            MapActivity_Main(onSwitch = {
                startActivity(Intent(this@MapActivity, MainActivity::class.java));
                finish();
            }, locationPermissionGranted);
        }

        /*val spinnerId = findViewById<Spinner>(R.id.spinId);
        val colors = arrayOf("Place 1", "Second Place", "Place.3", "4th Place");
        val arrayAdp = ArrayAdapter(this@MapActivity, android.R.layout.simple_spinner_dropdown_item, colors)
        spinnerId.adapter = arrayAdp;*/

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
                }
            }
        }
    }

    /*override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map!!.addMarker(MarkerOptions().position(userLocationState.value).title("Toronto"))
        map!!.moveCamera(CameraUpdateFactory.newLatLng(userLocationState.value))
    }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity_Main(onSwitch: () -> Unit, context: Context) {


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
                context = context
            )
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapActivity_Main(onSwitch: () -> Unit, isLocationEnabled: Boolean) {
    val weatherViewModel: WeatherViewModel = koinViewModel()
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

            var expanded by remember { mutableStateOf(false) }
            val items = weatherViewModel.getAllWeather()
            var selectedItem by remember { mutableStateOf(items[0]) }


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
                                    selectedItem = item
                                    userLocationState.value = item.latLng
                                    expanded = false
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
        position = CameraPosition.fromLatLngZoom(userLocationState.value, 14f)
    }

    // Track when the camera needs an update
    var cameraNeedsUpdate by remember { mutableStateOf(cameraNeedsUpdate) }

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

        // Add a marker at the user's location
        Marker(
            state = MarkerState(position = userLocationState.value),
            title = "You are here",
            snippet = "Current Location"
        )

        // Zoom in on the marker when the option changes
        cameraPositionState.move(update = CameraUpdateFactory.newLatLngZoom(userLocationState.value, 14f))

    }

}




/*class MapActivity : AppCompatActivity() {
    private lateinit var map: GoogleMap
    private lateinit var autocompleteFragment:AutocompleteSupportFragment
    private var startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                Log.i(
                    TAG, "Place: ${place.name}, ${place.id}"
                )
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // The user canceled the operation.
            Log.i(TAG, "User canceled autocomplete")
        }
    }//*/



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places API with your API key
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "@string/google_map_api_key")
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(

                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL

            )
        )

        // Display the fetched information after clicking on one of the options
        /*autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                // Text view where we will
                // append the information that we fetch
                val textView = findViewById<TextView>(R.id.tv1)

                // Information about the place
                val name = place.displayName
                val address = place.formattedAddress
                val phone = place.nationalPhoneNumber?.toString()
                val latlng = place.latLng
                val latitude = latlng?.latitude
                val longitude = latlng?.longitude

                val isOpenStatus : String = if(place.isOpen == true){
                    "Open"
                } else {
                    "Closed"
                }

                val rating = place.rating
                val userRatings = place.userRatingsTotal

                textView.text = "Name: $name \nAddress: $address \nPhone Number: $phone \n" +
                        "Latitude, Longitude: $latitude , $longitude \nIs open: $isOpenStatus \n" +
                        "Rating: $rating \nUser ratings: $userRatings"
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext,"Some error occurred", Toast.LENGTH_SHORT).show()
            }
        })*/

        // Set up a PlaceSelectionListener to handle the response.
        /*autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })*/



        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startAutocomplete.launch(intent);

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
        }

        setContent {

            var locationPermissionGranted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(
                        this@MapActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            // Display Google Map with the current location
            MapActivity_Main(onSwitch = {
                startActivity(Intent(this@MapActivity, MainActivity::class.java));
                finish();
            }, userLocationState.value, locationPermissionGranted);



            /*autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
            autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
            autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
                override fun onError(p0: Status) {
                    Toast.makeText(this@MapActivity, "Some Error in Search", Toast.LENGTH_SHORT).show()
                }

                override fun onPlaceSelected(place: Place) {
                    //val address = place.address
                    //val id = place.id
                    val latLng = place.latLng
                    userLocationState = mutableStateOf(latLng);
                }
            })*/

        }
        // Get the AutocompleteSupportFragment
        /*val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.autocomplete_fragment, autocompleteFragment)
            .commit()


        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )

        // Set a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Handle the selected place
                Log.i("PlaceSelected", "Place: ${place.name}, ${place.id}, ${place.latLng}")
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                // Handle the error
                Log.e("PlaceError", "An error occurred: $status")
            }
        })*/
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
                }
            }
        }
    }

    /*override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map!!.addMarker(MarkerOptions().position(userLocationState.value).title("Toronto"))
        map!!.moveCamera(CameraUpdateFactory.newLatLng(userLocationState.value))
    }*/
}*/







/*class CreateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            /*CreateActivity_Main(onCreatedTask = {
                startActivity(Intent(this@CreateActivity, MainActivity::class.java));
                finish();
            });*/
        }
    }
}*/

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeActivity_Main(onEditTask: () -> Unit, onCreateTask: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Tasks")
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Task");
            }
        },
        content =  { paddingValues ->
            TaskList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onEditTask
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivity_Main(onCreatedTask: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Create a new Task")
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        content =  { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                var name by remember { mutableStateOf("") };
                var description by remember { mutableStateOf("") };
                var dueDate by remember { mutableStateOf("") };
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                );
                Spacer(modifier = Modifier.height(8.dp));
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 10
                );
                Spacer(modifier = Modifier.height(16.dp));
                TextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                );
                Spacer(modifier = Modifier.height(16.dp));
                Button(onClick = {
                    onCreatedTask();
                    //Add Task to Repositiory

                }) {
                    Text("Add Task");
                }
            }
        }
    )
}*/










/*import android.Manifest
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Suppress("DEPRECATION")
class LandmarkDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var menu: Menu? = null
    private lateinit var locationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_detail)
        setupMapFragment()
        setupLocationClient()
        setupFloatingActionButton()
    }
    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }    private fun setupLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }    private fun setupFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            handleLocationPermission()
        }
    }
    private fun handleLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }
    }
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                map.isMyLocationEnabled = true
                startLocationUpdates()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Failed to enable location layer.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } ?: run {
                    Toast.makeText(applicationContext, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission is required to show your current location on the map.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val landmarkName = intent.getStringExtra("name") ?: "Landmark"
        map.uiSettings.isMyLocationButtonEnabled = false
        map.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title(landmarkName))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_options_menu, menu)
        this.menu = menu
        updateToggleItem()
        return true
    }
    private fun updateToggleItem() {
        menu?.findItem(R.id.action_toggle_view)?.let {
            if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                it.title = "Switch to Satellite"
                it.icon = getDrawable(R.drawable.ic_satellite)
            } else {
                it.title = "Switch to Map"
                it.icon = getDrawable(R.drawable.ic_map)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_view -> {
                toggleMapType()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun toggleMapType() {
        map.mapType = if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) GoogleMap.MAP_TYPE_SATELLITE else GoogleMap.MAP_TYPE_NORMAL
        updateToggleItem()
    }
}*/