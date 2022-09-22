package com.morales.nectar.data.remote

import com.morales.nectar.data.remote.responses.Plant
import com.morales.nectar.data.remote.responses.PlantList
import retrofit2.http.GET
import retrofit2.http.Path

interface PlantsApi {

    @GET("plant/user/{id}")
    suspend fun getPlantListByUserId(
        @Path("id") id: Int
    ): PlantList

    @GET("plant/{id}")
    suspend fun getPlantById(
        @Path("id") id: Int
    ): Plant
}