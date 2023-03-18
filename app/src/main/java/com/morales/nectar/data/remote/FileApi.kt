package com.morales.nectar.data.remote

import com.morales.nectar.data.remote.requests.plants.DeleteImageRequest
import com.morales.nectar.data.remote.responses.FileUploadResponse
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface FileApi {

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