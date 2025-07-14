package com.angad.zeptoclone.data.api

import com.angad.zeptoclone.data.models.location.NominationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NominationService {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Header("User-Agent") userAgent: String = "Zepto/1.0 (angadgupta840017@gmail.com)"
    ): NominationResponse

    @GET("search")
    suspend fun searchNearBy(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 5,
        @Header("User-Agent") userAgent: String = "Zepto/1.0 (angadgupta840017@gmail.com)"
    ): List<NominationResponse>

    @GET("search")
    suspend fun searchInBoundingBox(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("viewbox") viewBox: String,
        @Query("bounded") bounded: Int = 1,
        @Header("User-Agent") userAgent: String = "Zepto/1.0 (angadgupta840017@gmail.com)"
    ): List<NominationResponse>

}