package com.morales.nectar.repository

import com.morales.nectar.data.remote.AuthApi
import com.morales.nectar.data.remote.requests.auth.LoginRequest
import com.morales.nectar.data.remote.responses.LoginResponse
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class AuthRepository @Inject constructor(private val api: AuthApi) {

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return executeRequest { api.login(LoginRequest(email, password)) }
    }
}