package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class TakenUsernameResponse(@SerializedName("httpStatus")
                                 val httpStatus: Int = 0,
                                 @SerializedName("messages")
                                 val messages: List<String>? = null,
                                 @SerializedName("content")
                                 val content: TakenUsernameCheck)