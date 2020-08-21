package com.londogard.textgen.penalties

import kotlin.math.abs
import kotlin.math.min

/**
 * [[NgramPenalty]] is a penalty for repeating a Ngram of size ngram
 *  penalty = 0.2 means that you retain 20 % of the probability (i.e. prob(history) * 0.2
 */
class NgramPenalty(val ngram: Int, val penalty: Double = 1.0) : Penalty {
    override fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>> =
        entries.map { (index, score) -> index to score * ngramPenalty(history + listOf(index), ngram, penalty) }

    fun ngramPenalty(tokens: List<Int>, n: Int = 2, penalty: Double = 1.0): Double {
        if (tokens.size <= n) return 0.0

        val pen = min(abs(penalty), 1.0)
        val ngram = tokens.takeLast(n)

        return tokens
            .dropLast(1)
            .windowed(n) { if (it == ngram) 1 else 0 }
            .sum() * pen
    }
}