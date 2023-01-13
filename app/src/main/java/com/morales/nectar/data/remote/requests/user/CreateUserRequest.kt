package com.morales.nectar.data.remote.requests.user

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("username")
    val username: String
)

