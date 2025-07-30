package com.angad.zeptoclone.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.data.models.location.NominationResponse
import com.angad.zeptoclone.data.repository.LocationRepository
import com.angad.zeptoclone.data.repository.LocationRepositoryImpl
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    //    State for the full OSM response
    private val _osmResponse = MutableStateFlow<NominationResponse?>(null)
    val osmResponse = _osmResponse.asStateFlow()

    //    State for nearby places
    private val _nearbyPlaces = MutableStateFlow<List<NominationResponse>?>(null)
    val nearbyPlaces = _nearbyPlaces.asStateFlow()

    //    State for formatted address (to be displayed in the UI)
    private val _userAddress = MutableStateFlow("")
    val userAddress = _userAddress.asStateFlow()

    //    State for delivery time (could be calculated on based on location)
    private val _deliveryTime = MutableStateFlow("10 Mins")
    val deliveryTime = _deliveryTime.asStateFlow()

    //    Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    //    Error state
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    //    Current Location
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    //    Flag to track if we have loaded from cache already
    private var cacheLoaded = false

    init {
        //    Immediately the load cached user location
        viewModelScope.launch {
            //    Cast to implementation type to access the cached location property
            (locationRepository as? LocationRepositoryImpl)?.cachedLocation?.collect { cachedResponse ->
                cachedResponse?.let {
                    _osmResponse.value = it
                    _userAddress.value = it.display_name ?: "Location Found"
                    updateDeliveryTime(it)
                    cacheLoaded = true
                }
            }
        }
    }

    fun updateUserLocation() {
        viewModelScope.launch {
            //    If we already have cached location, don't show the full loading indicator, just silently update in background
            val showLoadingIndicator = !cacheLoaded || _userAddress.value.isEmpty()
            if (showLoadingIndicator) {
                _isLoading.value = true
            }
            _error.value = null

            try {
                val location = getCurrentLocation()
                if (location != null) {
                    _currentLocation.value = location

                    //    Get address from coordinates
                    locationRepository.getAddressFromCoordinates(
                        latitude = location.latitude,
                        longitude = location.longitude
                    ).onSuccess { response ->
                        _osmResponse.value = response
                        _userAddress.value = response.display_name ?: "Location Found"
                        updateDeliveryTime(response)

                        //    Search for nearBy places
                        //    "amenity" is a key category in OSM(open street map) that represents the type of place
                        searchNearbyPlaces("amenity")
                    }.onFailure { e ->
                        _error.value = "Error getting address: ${e.message}"
                    }
                } else {
                    _error.value = "Location not found"
                }
            } catch (e: Exception) {
                _error.value = "Error getting location: ${e.message}"
            } finally {
                if (showLoadingIndicator) {
                    _isLoading.value = false
                } else {
                    //    This ensures the small loading indicator goes away after background refresh completes
                    _isLoading.value = false
                }
            }
        }
    }

    //    Function that search for places that near the current location
    private fun searchNearbyPlaces(query: String, limit: Int = 5) {
        viewModelScope.launch {
            _currentLocation.value?.let { location ->
                val lat = location.latitude
                val lon = location.longitude

                val boundingBox = calculateBoundingBox(lat, lon, 1.0)

                locationRepository.searchInArea(
                    query = query,
                    minLon = boundingBox.first, //  Western limit
                    minLat = boundingBox.second,    //  Southern limit
                    maxLon = boundingBox.third,     //  Eastern limit
                    maxLat = boundingBox.fourth     //  Northern limit
                ).onSuccess { places ->
                    _nearbyPlaces.value = places
                }.onFailure { e ->
                    _error.value = "Failed to find nearby places: ${e.message}"
                }
            }
        }
    }

    /*
    * Calculate the bounding box around a point (lat/lon)
    * @param lat Latitude of the center point
    * @param lon Longitude of the center point  t
    * @param radiusKm Radius in kilometers
    * @return Quadruple of (minLon, minLat, maxLon, maxLat)
    * */

    private fun calculateBoundingBox(
        lat: Double,
        lon: Double,
        radiusKm: Double
    ): Quadruple<Double, Double, Double, Double> {
        val latChange = radiusKm / 110.574
        val lonChange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)))

        val minLat = lat - latChange
        val maxLat = lat + latChange
        val minLon = lon - lonChange
        val maxLon = lon + lonChange

        return Quadruple(minLon, minLat, maxLon, maxLat)
    }

    //    Get the current device location using FusedLocationProviderClient
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _error.value = "Location permission not granted"
                return null
            }
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: Exception) {
            _error.value = "Location error: ${e.message}"
            null
        }
    }

    //    Updates the delivery time estimation based on the location
    private fun updateDeliveryTime(response: NominationResponse) {
        //    This is a placeholder in a real app, we would need to calculate based on location
        _deliveryTime.value = "10 Mins"
    }

}

//    Helper class for returning 4 values
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)