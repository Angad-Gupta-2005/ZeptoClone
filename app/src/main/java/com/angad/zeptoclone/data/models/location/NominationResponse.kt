package com.angad.zeptoclone.data.models.location

data class NominationResponse(
    val place_id: Long? = null,
    val licence: String? = null,
    val osm_type: String? = null,
    val osm_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    val category: String? = null,
    val type: String? = null,
    val place_rank: Int? = null,
    val importance: Double? = null,
    val addresstype: String? = null,
    val name: String? = null,
    val display_name: String? = null,
    val boundingbox: List<String>? = null,
    val address: Address? = null
)
