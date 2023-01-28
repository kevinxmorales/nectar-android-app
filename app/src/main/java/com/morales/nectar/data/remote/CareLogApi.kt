package com.morales.nectar.data.remote

import com.morales.nectar.data.models.CareLog
import com.morales.nectar.data.remote.requests.care.CareLogRequest
import com.morales.nectar.data.remote.requests.care.UpdateCareLogRequest
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CareLogApi {

    @GET("/api/v1/plant-care/user/{id}")
    suspend fun getCareLogsByUserId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<List<CareLog>>>

    @GET("plant-care/{id}")
    suspend fun getCareLogsByPlantId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<List<CareLog>>>

    @GET("plant-care/id/{id}")
    suspend fun getCareLogsById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<CareLog>>

    @POST("plant-care")
    suspend fun createCareLogEntry(
        @Header("Authorization") token: String,
        @Body careLogEntry: CareLogRequest
    ): Response<NectarResponseEntity<Any>>

    @PUT("plant-care/{id}")
    suspend fun updateCareLogEntry(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body careLog: UpdateCareLogRequest
    ): Response<NectarResponseEntity<Any>>

    @DELETE("plant-care/{id}")
    suspend fun deleteCareLogEntry(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<Any>>

}