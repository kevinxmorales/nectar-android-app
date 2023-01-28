package com.morales.nectar.data.remote.requests.care

import com.google.gson.annotations.SerializedName

data class UpdateCareLogRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("notes")
    val notes: String? = "",
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = false,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = false,
)
