package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class UserDataResponse(@SerializedName("httpStatus")
                            val httpStatus: Int = 0,
                            @SerializedName("messages")
                            val messages: List<String>?,
                            @SerializedName("content")
                            val content: UserData)