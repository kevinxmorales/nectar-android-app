package com.morales.nectar.data.remote.requests.plants

import com.google.gson.annotations.SerializedName

data class DeleteImageRequest(
    @SerializedName("uri")
    val uri: String
)
