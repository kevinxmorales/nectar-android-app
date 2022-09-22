package com.morales.nectar.repository

import android.util.Log
import com.morales.nectar.data.remote.UserApi
import com.morales.nectar.data.remote.requests.CreateUserRequest
import com.morales.nectar.data.remote.responses.TakenUsernameResponse
import com.morales.nectar.data.remote.responses.UserDataResponse
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

private const val TAG = "UserRepository"

@ActivityScoped
class UserRepository
    @Inject constructor(private val api: UserApi) {

    suspend fun checkIfUsernameIsTaken(username: String): Resource<TakenUsernameResponse> {
        val response = try {
            api.checkIfUsernameIsTaken(username = username)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
            return Resource.Error("An unknown error occurred.")
        }
        return Resource.Success(response)
    }

    suspend fun getUserWithAuthId(authId: String): Resource<UserDataResponse> {
        val response = try {
            api.getUserWithAuthId(authId = authId)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
            return Resource.Error("An unknown error occurred.")
        }
        return Resource.Success(response)
    }

    suspend fun createUser(newUser: CreateUserRequest): Resource<UserDataResponse> {
        val response = try {
            Log.i(TAG, newUser.toString())
            api.createUser(newUser = newUser)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
            return Resource.Error("An unknown error occurred.")
        }
        return Resource.Success(response)
    }

    suspend fun updateUser(id: Int, updatedUser: CreateUserRequest): Resource<UserDataResponse> {
        val response = try {
            Log.i(TAG, updatedUser.toString())
            api.updateUser(id = id, newUser = updatedUser)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
            return Resource.Error("An unknown error occurred.")
        }
        return Resource.Success(response)
    }
}