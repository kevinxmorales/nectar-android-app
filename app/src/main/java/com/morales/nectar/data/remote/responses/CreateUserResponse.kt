package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName
import com.morales.nectar.data.models.UserData

data class CreateUserResponse(
    @SerializedName("user")
    val user: UserData,
    @SerializedName("sessionToken")
    val sessionToken: String
)