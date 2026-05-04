package com.eeman.stockscout.data.mappers

import com.eeman.stockscout.data.local.AliasEntity
import com.eeman.stockscout.data.local.ItemEntity
import com.eeman.stockscout.data.remote.dto.ItemDto
import com.eeman.stockscout.data.models.Alias
import com.eeman.stockscout.data.models.AliasType
import com.eeman.stockscout.data.models.Item

// DTO → Entity
fun ItemDto.toItemEntity() = ItemEntity(
    itemCode = itemCode,
    name = name,
    uom = uom,
    onHandQty = onHandQty
)

fun ItemDto.toAliasEntities(): List<AliasEntity> = aliases.map { dto ->
    AliasEntity(
        itemCode = itemCode,
        type = dto.type,
        value = dto.value
    )
}

// Entity → Domain
fun ItemEntity.toDomain(aliases: List<AliasEntity>) = Item(
    itemCode = itemCode,
    name = name,
    uom = uom,
    onHandQty = onHandQty,
    aliases = aliases.map { it.toDomain() }
)

fun AliasEntity.toDomain() = Alias(
    type = runCatching { AliasType.valueOf(type) }.getOrDefault(AliasType.TEXT),
    value = value
)