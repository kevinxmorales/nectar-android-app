package com.morales.nectar.data.remote

import com.morales.nectar.data.models.UserData
import com.morales.nectar.data.remote.requests.auth.LoginRequest
import com.morales.nectar.data.remote.requests.user.CreateUserRequest
import com.morales.nectar.data.remote.requests.user.UpdateUserRequest
import com.morales.nectar.data.remote.responses.CreateUserResponse
import com.morales.nectar.data.remote.responses.FileUploadResponse
import com.morales.nectar.data.remote.responses.LoginResponse
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import com.morales.nectar.data.remote.responses.TakenUsernameCheck
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<NectarResponseEntity<LoginResponse>>

    @GET("user/username-check/is-taken")
    suspend fun checkIfUsernameIsTaken(
        @Query("username") username: String
    ): Response<NectarResponseEntity<TakenUsernameCheck>>

    @GET("user/id/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NectarResponseEntity<UserData>>

    @POST("user")
    suspend fun createUser(
        @Body newUser: CreateUserRequest
    ): Response<NectarResponseEntity<CreateUserResponse>>

    @PUT("user/id/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body newUser: UpdateUserRequest
    ): Response<NectarResponseEntity<UserData>>

    @Multipart
    @POST("user/id/{id}/image")
    suspend fun updateProfileImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Path("id") id: String
    ): Response<NectarResponseEntity<FileUploadResponse>>
}