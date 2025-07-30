package com.angad.zeptoclone.data.repository

import android.content.Context
import com.angad.zeptoclone.data.api.NominationService
import com.angad.zeptoclone.data.models.location.NominationResponse
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val nominationService: NominationService,
    @ApplicationContext private val context: Context
): LocationRepository {

    companion object{
        private const val LOCATION_CACHE = "location_cache"
        private const val LOCATION_KEY = "user_location"
    }

    private val gson = Gson()
    private val prefs = context.getSharedPreferences(LOCATION_CACHE, Context.MODE_PRIVATE)
    private val _cachedLocation = MutableStateFlow<NominationResponse?>(null)
    val cachedLocation = _cachedLocation.asStateFlow()

    init {
        loadCachedLocation()
    }

    private fun loadCachedLocation() {
        val cachedJson = prefs.getString(LOCATION_KEY, null)
        if (!cachedJson.isNullOrEmpty()){
            try {
                val cachedResponse = gson.fromJson(cachedJson, NominationResponse::class.java)
                _cachedLocation.value = cachedResponse
            } catch (e: Exception){
            //    Handle parsing error, could not load cache
            }
        }
    }

    private fun cacheLocation(response: NominationResponse){
        try {
            val json = gson.toJson(response)
            prefs.edit().putString(LOCATION_KEY, json).apply()
            _cachedLocation.value = response
        } catch (e: Exception){
            //  Handle serialization error, could not load cache
        }
    }


    override suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<NominationResponse> {
        _cachedLocation.value?.let {
            return Result.success(it)
        }
        return withContext(Dispatchers.IO){
            try {
                val response = nominationService.reverseGeocode(latitude, longitude)
                cacheLocation(response)
                Result.success(response)
            } catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun searchNearByPlaces(
        query: String,
        limit: Int
    ): Result<List<NominationResponse>> {
        return withContext(Dispatchers.IO){
            try {
                val response = nominationService.searchNearBy(query, limit = limit)
                Result.success(response)
            } catch (e: Exception){
                Result.failure(e)
            }
        }
    }

    override suspend fun searchInArea(
        query: String,
        minLon: Double,
        minLat: Double,
        maxLon: Double,
        maxLat: Double
    ): Result<List<NominationResponse>> {
        return withContext(Dispatchers.IO){
            try {
                val viewBoxParam = "$minLon,$minLat,$maxLon,$maxLat"
                val response = nominationService.searchInBoundingBox(query, viewBox = viewBoxParam)
                Result.success(response)
            } catch (e: Exception){
                Result.failure(e)
            }
        }
    }
}