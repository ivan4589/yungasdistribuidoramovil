package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.location.LocationDto
import retrofit2.Response
import retrofit2.http.GET

interface LocationApi {
    @GET("locations")
    suspend fun getLocations(): Response<List<LocationDto>>
}
