package com.morales.nectar.repository

import com.morales.nectar.data.models.UserData
import com.morales.nectar.data.remote.UserApi
import com.morales.nectar.data.remote.requests.user.CreateUserRequest
import com.morales.nectar.data.remote.requests.user.UpdateUserRequest
import com.morales.nectar.data.remote.responses.TakenUsernameCheck
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

private const val TAG = "UserRepository"

@ActivityScoped
class UserRepository
@Inject constructor(private val api: UserApi) {

    suspend fun checkIfUsernameIsTaken(username: String): Resource<TakenUsernameCheck> {
        return executeRequest { api.checkIfUsernameIsTaken(username) }
    }

    suspend fun getUserById(token: String, id: String): Resource<UserData> {
        return executeRequest { api.getUserById(token, id) }
    }

    suspend fun createUser(
        newUser: CreateUserRequest
    ): Resource<UserData> {
        return executeRequest { api.createUser(newUser) }
    }

    suspend fun updateUser(
        token: String,
        id: String,
        updatedUser: UpdateUserRequest
    ): Resource<UserData> {
        return executeRequest { api.updateUser(token, id, updatedUser) }
    }
}

