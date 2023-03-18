package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName
import com.morales.nectar.data.models.PlantData

data class FileUploadResponse(
    @SerializedName("plant") val plant: PlantData? = null,
    @SerializedName("imageUrl") val imageUrl: String
)
