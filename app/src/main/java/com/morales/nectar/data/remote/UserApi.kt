package com.morales.nectar.data.remote

import com.morales.nectar.data.models.UserData
import com.morales.nectar.data.remote.requests.user.CreateUserRequest
import com.morales.nectar.data.remote.requests.user.UpdateUserRequest
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import com.morales.nectar.data.remote.responses.TakenUsernameCheck
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

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
    ): Response<NectarResponseEntity<UserData>>

    @PUT("user/id/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body newUser: UpdateUserRequest
    ): Response<NectarResponseEntity<UserData>>
}