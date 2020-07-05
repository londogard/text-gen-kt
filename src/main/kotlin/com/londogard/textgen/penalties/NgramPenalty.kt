package com.londogard.textgen.penalties

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

class NgramPenalty(ngram: Int): Penalty {
    override fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>> {

        TODO("Not yet implemented")
    }
}