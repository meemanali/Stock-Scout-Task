package com.eeman.stockscout.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemDto(
    @SerializedName("itemCode")   val itemCode: String,
    @SerializedName("name")       val name: String,
    @SerializedName("uom")        val uom: String,
    @SerializedName("onHandQty")  val onHandQty: Int,
    @SerializedName("aliases")    val aliases: List<AliasDto>
)