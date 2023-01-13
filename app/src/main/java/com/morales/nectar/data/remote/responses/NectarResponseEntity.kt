package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class NectarResponseEntity<T>(
    @SerializedName("message")
    val message: String?,
    @SerializedName("content")
    val content: T
)