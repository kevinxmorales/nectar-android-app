package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName
import com.morales.nectar.data.models.PlantData

data class GetPlantsResponse(
    @SerializedName("plants")
    val plants: List<PlantData>
)
