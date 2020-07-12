package com.londogard.textgen.normalization

interface Normalization {
    val temperature: Double

    // Normalize probabilities in a given fashion which ends up with total p = 1.0.
    //  E.g. [0.2, 0.3] -> [0.4, 0.6] is a way of normalization.
    fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>>

    // The List<Pair> func is 4-10 times slower.
    fun normalize(probabilities: DoubleArray): DoubleArray
}
