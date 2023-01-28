package com.morales.nectar.data.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class CareLog(
    @SerializedName("id")
    val id: String,
    @SerializedName("plantId")
    val plantId: String,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean = false,
    @SerializedName("wasWatered")
    val wasWatered: Boolean = false,
    @SerializedName("careDate")
    val careDate: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("plantImage")
    val plantImage: String = "",
    @SerializedName("plantName")
    val plantName: String
) {
    fun toCareLogParcel(): CareLogParcel {
        return CareLogParcel(
            id = id,
            plantId = plantId,
            notes = notes,
            wasFertilized = wasFertilized,
            wasWatered = wasWatered,
            careDate = careDate,
            createdAt = createdAt,
            plantImage = plantImage,
            plantName = plantName
        )
    }
}
