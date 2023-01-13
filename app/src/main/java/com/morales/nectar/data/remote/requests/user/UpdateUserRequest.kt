package com.morales.nectar.data.remote.requests.user

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("email")
    val email: String
)