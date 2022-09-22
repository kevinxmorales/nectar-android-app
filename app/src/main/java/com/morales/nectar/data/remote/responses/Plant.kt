package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class Plant(
    @SerializedName("createdAt")
    val createdAt: String = "",
    @SerializedName("images")
    val images: List<Images>?,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("userId")
    val userId: Int = 0,
    @SerializedName("categoryId")
    val categoryId: String = "",
    @SerializedName("careLogEntries")
    val careLogEntries: List<CareLogEntry>?,
)
