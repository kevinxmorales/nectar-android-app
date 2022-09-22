package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class TakenUsernameCheck(@SerializedName("isTaken")
                              val isTaken: Boolean = false,
                              @SerializedName("username")
                              val username: String = "")