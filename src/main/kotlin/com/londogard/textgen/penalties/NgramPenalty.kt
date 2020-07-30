package com.londogard.textgen.penalties

import kotlin.math.abs
import kotlin.math.min

fun ngramPenalty(tokens: List<Int>, n: Int = 2, penalty: Double = 1.0): Double {
    if (tokens.size <= n) return 0.0

    val pen = min(abs(penalty), 1.0)
    val ngram = tokens.takeLast(n)

    return tokens
        .dropLast(1)
        .windowed(n) { if (it == ngram) 1 else 0 }
        .sum() * pen
}

// penalty = 0.2, loose 80 % of probability (i.e. keep 20%)
class NgramPenalty(val ngram: Int, val penalty: Double = 1.0) : Penalty {
    override fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>> =
        entries.map { (index, score) -> index to score * ngramPenalty(history + listOf(index), ngram, penalty) }
}