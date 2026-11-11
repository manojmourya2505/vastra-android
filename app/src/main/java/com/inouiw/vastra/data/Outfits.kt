package com.inouiw.vastra.data

import kotlin.math.abs

data class Outfit(val label: String, val items: List<Product>, val reason: String = "") {
    val total: Int get() = items.sumOf { it.price }
}

object Outfits {
    private val occasionStyle = mapOf("dinner" to "formal", "office" to "formal", "weekend" to "casual", "nightwear" to "lounge")

    /** Up to three outfits: the one using most of the budget, one in the middle, and the cheapest. */
    fun build(plan: Plan, catalog: List<Product>): List<Outfit> {
        val style = occasionStyle[plan.occ] ?: "formal"
        val available = catalog.filter { it.stock > 0 && it.style == style }
        val dresses = available.filter { it.category == "Dresses" }
        val tops = available.filter { it.category == "Tops" }
        val bottoms = available.filter { it.category == "Bottoms" }
        val shoes = available.filter { it.category == "Footwear" }

        val candidates = mutableListOf<Outfit>()
        for (d in dresses) for (s in shoes) candidates += Outfit("", listOf(d, s))
        for (t in tops) for (b in bottoms) for (s in shoes) candidates += Outfit("", listOf(t, b, s))

        val affordable = candidates.filter { it.total <= plan.budget }
        if (affordable.isEmpty()) return emptyList()

        val high = affordable.maxBy { it.total }
        val low = affordable.minBy { it.total }
        val rest = affordable.filter { it !== high && it !== low }
        val mid = rest.minByOrNull { abs(it.total - (high.total + low.total) / 2) } ?: low

        return listOfNotNull(
            high.copy(label = "Uses most of your budget"),
            if (mid !== high && mid !== low) mid.copy(label = "Middle choice") else null,
            if (low !== high) low.copy(label = "Budget pick") else null,
        )
    }
}
