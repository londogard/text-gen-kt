package com.londogard.textgen.penalties

import smile.nlp.ngram
import kotlin.math.abs
import kotlin.math.min

fun ngramPenalty(tokens: List<Int>, n: Int = 2, penalty: Double = 1.0): Double {
    if (tokens.size <= n) return 0.0

    val pen = min(abs(penalty), 1.0)
    val ngram = tokens.takeLast(n)

    return tokens    // TODO performance improvement by sliding over list rather than creating sublists
        .dropLast(1)
        .asSequence()
        .windowed(n)
        .count { it == ngram } * pen
}

// penalty = 0.2, loose 80 % of probability (i.e. keep 20%)
class NgramPenalty(val ngram: Int, val penalty: Double = 1.0) : Penalty {
    override fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>> =
        entries.map { (index, score) -> index to score * ngramPenalty(history + listOf(index), ngram, penalty) }
}