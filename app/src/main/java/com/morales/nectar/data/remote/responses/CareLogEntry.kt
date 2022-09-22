package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class CareLogEntry(
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("wasWatered")
    val wasWatered: Boolean? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("plantId")
    val plantId: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("wasFertilized")
    val wasFertilized: Boolean? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    fun toMap() = mapOf(
        "id" to id,
        "uid" to uid,
        "date" to date,
        "notes" to notes,
        "plantId" to plantId,
        "wasFertilized" to wasFertilized,
        "wasWatered" to wasWatered,
        "timestamp" to timestamp,
    )
}