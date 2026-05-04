package com.eeman.stockscout.data.models

// Domain model
data class Item(
    val itemCode: String,
    val name: String,
    val uom: String,
    val onHandQty: Int,
    val aliases: List<Alias>
)

data class Alias(
    val type: AliasType,  // UPC_A, EAN_13, GS1, TEXT
    val value: String
)

enum class AliasType { UPC_A, EAN_13, GS1, TEXT }

// For GS1 parsed result
data class Gs1ParsedData(
    val gtin: String,
    val lot: String?,
    val expiry: String?,
    val quantity: String?
)