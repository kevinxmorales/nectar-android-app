package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class PlantList(@SerializedName("httpStatus")
                     val httpStatus: Int = 0,
                     @SerializedName("content")
                     val content: List<Plant>?)