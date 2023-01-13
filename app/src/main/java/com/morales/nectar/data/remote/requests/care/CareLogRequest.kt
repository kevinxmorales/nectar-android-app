package com.morales.nectar.data.remote.requests.care

import com.google.gson.annotations.SerializedName

data class CareLogRequest(
    @SerializedName("plantId")
    val plantId: String,
    @SerializedName("notes")
    val notes: String? = "",
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = false,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = false,
)