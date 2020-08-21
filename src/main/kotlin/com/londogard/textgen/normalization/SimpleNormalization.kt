package com.londogard.textgen.normalization

/**
 * [[SimpleNormalization]] is the simplest normalization which just takes divide each probability by total sum.
 */
class SimpleNormalization(override val temperature: Double = 0.0) : Normalization {
    override fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
        val sum = probabilities.sumByDouble { (_, score) -> score }
        return probabilities.map { (index, score) -> index to score / sum }
    }

    override fun normalize(probabilities: DoubleArray): DoubleArray {
        val sum = probabilities.sum()
        for (i in probabilities.indices) probabilities[i] /= sum
        return probabilities
    }
}