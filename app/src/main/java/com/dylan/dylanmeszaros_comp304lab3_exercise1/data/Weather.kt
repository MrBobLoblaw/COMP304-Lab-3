package com.dylan.dylanmeszaros_comp304lab3_exercise1.data

import com.google.android.gms.maps.model.LatLng

data class Weather (
    var id: Int,
    var placeName: String,
    var description: String,
    var latLng: LatLng,
    var temperature: String
)