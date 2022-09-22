package com.morales.nectar.data.models

data class UserData(
    var id: Int? = null,
    var authId: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var username: String? = null,
    var imageUrl: String? = null,
    var following: List<String>? = null
) {
    fun toMap() = mapOf(
        "authId" to authId,
        "firstName" to firstName,
        "lastName" to lastName,
        "username" to username,
        "imageUrl" to imageUrl,
        "following" to following
    )
}
