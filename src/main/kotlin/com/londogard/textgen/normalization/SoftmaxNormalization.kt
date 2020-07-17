package com.londogard.textgen.normalization

import kotlin.math.exp

/**
 * Softmax with temperature as defined in Ari Holtzman et al. (2019) - https://arxiv.org/pdf/1904.09751.pdf (ch. 3.3)
 */
class SoftmaxNormalization(override val temperature: Double) : Normalization {
    override fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
        val probs = probabilities.map { (_, score) -> score }.toDoubleArray()
        val normalized = normalize(probs)

        return List(probs.size) { i -> probabilities[i].first to normalized[i] }
    }

    override fun normalize(probabilities: DoubleArray): DoubleArray {
        for (i in probabilities.indices) probabilities[i] = exp(probabilities[i] / temperature)
        val sum = probabilities.sum()
        for (i in probabilities.indices) probabilities[i] /= sum
        return probabilities
    }
}