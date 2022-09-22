package com.morales.nectar.data.remote.requests

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(@SerializedName("name")
                    val name: String,
                    @SerializedName("email")
                    val email: String,
                    @SerializedName("authId")
                    val authId: String,
                    @SerializedName("username")
                    val username: String)

