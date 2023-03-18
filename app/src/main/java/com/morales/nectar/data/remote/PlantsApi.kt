package com.morales.nectar.data.remote

import com.morales.nectar.data.models.PlantData
import com.morales.nectar.data.remote.requests.plants.DeleteImageRequest
import com.morales.nectar.data.remote.responses.FileUploadResponse
import com.morales.nectar.data.remote.responses.GetPlantsResponse
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import com.morales.nectar.data.remote.responses.NewPlantData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface PlantsApi {

    @GET("plant/user/{id}")
    suspend fun getPlantListByUserId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<GetPlantsResponse>>

    @GET("plant/{id}")
    suspend fun getPlantById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<PlantData>>

    @GET("plant/search/{params}")
    suspend fun searchPlants(
        @Header("Authorization") token: String,
        params: String
    ): Response<NectarResponseEntity<List<PlantData>>>

    @POST("plant")
    suspend fun createPlant(
        @Header("Authorization") token: String,
        @Body newPlant: PlantData
    ): Response<NectarResponseEntity<PlantData>>

    @PUT("plant/{id}")
    suspend fun updatePlant(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body newPlant: PlantData
    ): Response<NectarResponseEntity<PlantData>>

    @DELETE("plant/{id}")
    suspend fun deletePlant(
        @Header("Authorization") token: String,
        @Path("id") plantId: String
    ): Response<NectarResponseEntity<NewPlantData>>

    @Multipart
    @POST("plant/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<NectarResponseEntity<FileUploadResponse>>

    @Multipart
    @POST("plant/image/plant-id/{id}")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part image: MultipartBody.Part
    ): Response<NectarResponseEntity<FileUploadResponse>>

    @PUT("plant/image/plant-id/{id}")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body deleteImageRequest: DeleteImageRequest
    ): Response<NectarResponseEntity<Any>>


}