package com.morales.nectar.data.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class CareLogEntry(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("plantId")
    val plantId: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = null,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = null,
    @SerializedName("date")
    val date: String? = Calendar.getInstance().time.toString(),
)