package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class NewPlantData(
    @SerializedName("id")
    val id: String
)