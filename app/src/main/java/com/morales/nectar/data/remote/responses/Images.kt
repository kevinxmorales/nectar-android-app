package com.morales.nectar.data.remote.responses

import com.google.gson.annotations.SerializedName

data class Images(@SerializedName("url")
                      val url: String = "",
                      @SerializedName("thumbnailUrl")
                      val thumbnailUrl: String = "")