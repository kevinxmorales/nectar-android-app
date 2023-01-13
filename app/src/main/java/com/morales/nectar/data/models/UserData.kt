package com.morales.nectar.data.models

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("plantCount")
    val plantCount: Int = 0,
    @SerializedName("image_url")
    var imageUrl: String? = "",
    @SerializedName("following")
    val following: List<String> = listOf(),
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("email")
    val email: String? = "",
    @SerializedName("username")
    val username: String? = ""
)
