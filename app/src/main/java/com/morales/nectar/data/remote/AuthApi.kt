package com.morales.nectar.data.remote

import com.morales.nectar.data.remote.requests.auth.LoginRequest
import com.morales.nectar.data.remote.responses.LoginResponse
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<NectarResponseEntity<LoginResponse>>
}