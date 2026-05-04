package com.eeman.stockscout.domain.resolver

import com.eeman.stockscout.data.models.Gs1ParsedData


/**
 * Parses GS1-128 / GS1 DataMatrix strings.
 *
 * Application Identifiers (AIs) we care about:
 *   (01) GTIN          — 14 digits, fixed length
 *   (10) Lot/Batch     — variable length, up to 20 chars, terminated by FNC1
 *   (17) Expiry date   — 6 digits YYMMDD, fixed length
 *   (21) Serial number — variable length, up to 20 chars
 *   (30) Quantity      — variable length, up to 8 digits
 *
 * Input may use:
 *   - Parenthesised AIs: "(01)12345678901231(10)LOT001(17)261231"
 *   - Raw concatenated : "011234567890123110LOT001\x1D17261231"
 *   - FNC1 separator   : \x1D or ]C1 prefix
 */
object Gs1Parser {

    // Matches parenthesised AI format: (NN)data or (NNN)data or (NNNN)data
    private val PAREN_AI = Regex("""\((\d{2,4})\)([^(]*)""")

    // Fixed-length AIs: (AI) → exact number of data characters
    private val FIXED_LENGTH = mapOf(
        "01" to 14,  // GTIN
        "17" to 6,   // Expiry YYMMDD
        "11" to 6,   // Production date
        "13" to 6,   // Packaging date
    )

    fun isGs1(input: String): Boolean {
        val cleaned = input.trimStart(']', 'C', '1').trim()
        if (cleaned.startsWith("(01)")) return true
        if (cleaned.startsWith("01") && cleaned.length >= 16) return true
        return PAREN_AI.containsMatchIn(cleaned)
    }

    fun parse(input: String): Gs1ParsedData {
        val cleaned = input
            .trimStart(']', 'C', '1')   // strip ]C1 prefix from scanner
            .replace("\u001D", "")       // strip FNC1 group separator

        val aiMap: Map<String, String> = if (cleaned.contains("(")) {
            parseParenthesised(cleaned)
        } else {
            parseRaw(cleaned)
        }

        val rawGtin = aiMap["01"] ?: ""

        return Gs1ParsedData(
            gtin = rawGtin.trimStart('0').ifEmpty { rawGtin },
            lot = aiMap["10"],
            expiry = aiMap["17"],
            quantity = aiMap["30"],
//            serialNumber = aiMap["21"]
        )
    }

    // "(01)12345678901231(10)LOT001(17)261231" → {01: "12345678901231", 10: "LOT001", 17: "261231"}
    private fun parseParenthesised(input: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        PAREN_AI.findAll(input).forEach { match ->
            val ai = match.groupValues[1]
            val data = match.groupValues[2].trim()
            map[ai] = data
        }
        return map
    }

    // Raw concatenated — walk through applying fixed/variable length rules
    private fun parseRaw(input: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var pos = 0

        while (pos < input.length) {
            // Try 4-digit AI first, then 3, then 2
            val ai = listOf(4, 3, 2)
                .firstOrNull { len ->
                    pos + len <= input.length &&
                            isKnownAi(input.substring(pos, pos + len))
                } ?: break

            val aiStr = input.substring(pos, pos + ai)
            pos += ai

            val fixedLen = FIXED_LENGTH[aiStr]
            if (fixedLen != null) {
                if (pos + fixedLen <= input.length) {
                    map[aiStr] = input.substring(pos, pos + fixedLen)
                    pos += fixedLen
                } else break
            } else {
                // Variable length — read until end (FNC1 already stripped)
                val remaining = input.substring(pos)
                map[aiStr] = remaining
                break
            }
        }

        return map
    }

    private fun isKnownAi(ai: String): Boolean =
        ai in setOf("01", "10", "17", "21", "30", "11", "13", "00", "02")
}