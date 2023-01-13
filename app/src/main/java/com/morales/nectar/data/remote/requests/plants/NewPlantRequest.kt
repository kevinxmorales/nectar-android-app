package com.morales.nectar.data.remote.requests.plants

import com.google.gson.annotations.SerializedName
import java.util.*

data class NewPlantRequest(
    @SerializedName("commonName")
    var commonName: String? = null,
    @SerializedName("scientificName")
    var scientificName: String? = null,
    @SerializedName("toxicity")
    var toxicity: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("images")
    val images: List<String>? = null,
    @SerializedName("createdAt")
    val createdAt: String? = Calendar.getInstance().time.toString(),
)