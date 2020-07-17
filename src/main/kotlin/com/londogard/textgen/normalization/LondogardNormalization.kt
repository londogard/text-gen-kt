package com.londogard.textgen.normalization

import kotlin.math.pow

/**
 * A new type of normalization that skews the data in a fashion close to the softmax, but a bit sharper.
 */
class LondogardNormalization(override val temperature: Double) : Normalization {
    override fun normalize(probabilities: DoubleArray): DoubleArray {
        for (i in probabilities.indices) probabilities[i] = probabilities[i].pow(1 / (1 - temperature))
        val sum = probabilities.sum()
        for (i in probabilities.indices) probabilities[i] /= sum
        return probabilities
    }

    override fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
        val probs = probabilities.map { (_, score) -> score }.toDoubleArray()
        val normalized = normalize(probs)

        return List(probs.size) { i -> probabilities[i].first to normalized[i] }
    }
}