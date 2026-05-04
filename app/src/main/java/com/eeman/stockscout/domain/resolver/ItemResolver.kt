package com.eeman.stockscout.domain.resolver

import com.eeman.stockscout.data.models.Item

class ItemResolver(private val items: List<Item>) {

    /**
     * Resolves any input string to a single Item.
     *
     * Resolution order:
     * 1. GS1 string → parse GTIN → match aliases
     * 2. Exact item code match (case-insensitive)
     * 3. Exact alias value match (normalised)
     * 4. null if nothing found
     */
    fun resolve(input: String): Item? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null

        if (Gs1Parser.isGs1(trimmed)) {
            val parsed = Gs1Parser.parse(trimmed)
            return findByNormalisedValue(parsed.gtin)
        }

        // Item code
//        items.find { it.itemCode.equals(trimmed, ignoreCase = true) }
//            ?.let { return it }
        items.find { it.itemCode.contains(trimmed, ignoreCase = true) }
            ?.let { return it }

        // Alias (normalise both sides — strip leading zeros for numeric)
        return findByNormalisedValue(trimmed)
    }

    private fun findByNormalisedValue(value: String): Item? {
        val normInput = normalise(value)
        return items.find { item ->
            item.aliases.any { alias ->
//                normalise(alias.value) == normInput
                normalise(alias.value).contains(normInput)
            }
        }
    }

    /**
     * Normalisation: for purely numeric strings strip leading zeros
     * so UPC-A "012345678905" matches GS1 GTIN "12345678905".
     * Text aliases are lowercased only.
     */
    private fun normalise(value: String): String {
        return if (value.all { it.isDigit() }) {
            value.trimStart('0').ifEmpty { "0" }
        } else {
            value.lowercase().trim()
        }
    }
}