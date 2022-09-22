package com.morales.nectar.data.remote.responses

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
    val id: Int = 0,
    @SerializedName("email")
    val email: String? = "",
    @SerializedName("authId")
    val authId: String? = "",
    @SerializedName("username")
    val username: String? = ""
) {

    fun toMap() = mapOf(
        "id" to id,
        "authId" to authId,
        "name" to name,
        "username" to username,
        "email" to email,
        "imageUrl" to imageUrl,
        "following" to following,
        "plantCount" to plantCount
    )

}
