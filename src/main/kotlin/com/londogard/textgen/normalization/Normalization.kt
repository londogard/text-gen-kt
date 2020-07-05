package com.londogard.textgen.normalization

interface Normalization {
    val temperature: Double
    fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>>
    // fun normalize(probabilities: List<Pair<Int,Double>>): List<Pair<Int, Double>>
    fun normalize(probabilities: DoubleArray): DoubleArray
}
