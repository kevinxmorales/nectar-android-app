package com.morales.nectar.data.remote

import com.morales.nectar.data.remote.requests.CreateUserRequest
import com.morales.nectar.data.remote.responses.TakenUsernameResponse
import com.morales.nectar.data.remote.responses.UserData
import com.morales.nectar.data.remote.responses.UserDataResponse
import retrofit2.http.*

interface UserApi {

    @GET("user/username-check/is-taken")
    suspend fun checkIfUsernameIsTaken(@Query("username") username: String): TakenUsernameResponse

    @GET("user/auth-id/{authId}")
    suspend fun getUserWithAuthId(@Path("authId") authId: String): UserDataResponse

    @POST("user")
    suspend fun createUser(@Body newUser: CreateUserRequest): UserDataResponse

    @PUT("user/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body newUser: CreateUserRequest): UserDataResponse
}