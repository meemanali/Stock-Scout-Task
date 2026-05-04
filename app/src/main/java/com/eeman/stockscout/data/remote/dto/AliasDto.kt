package com.eeman.stockscout.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AliasDto(
    @SerializedName("type")  val type: String,
    @SerializedName("value") val value: String
)