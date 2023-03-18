package com.morales.nectar.data.remote.requests.care

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CareLogRequest(
    @SerializedName("plantId")
    val plantId: String,
    @SerializedName("notes")
    val notes: String? = "",
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = false,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = false,
    @SerializedName("careDate")
    val careDate: String = LocalDate.now().format(DateTimeFormatter.RFC_1123_DATE_TIME).toString()
)