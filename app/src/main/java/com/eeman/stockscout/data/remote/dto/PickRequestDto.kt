package com.eeman.stockscout.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PickRequestDto(
    @SerializedName("itemCode")   val itemCode: String,
    @SerializedName("newQty")     val newQty: Int,
    @SerializedName("timestamp")  val timestamp: String  // ISO-8601
)