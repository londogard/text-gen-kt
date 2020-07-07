package com.londogard.textgen.normalization

interface Normalization {
    val temperature: Double
    // The List<Pair> func is 4-10 times slower.
    fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>>
    fun normalize(probabilities: DoubleArray): DoubleArray
}
