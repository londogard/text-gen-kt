package com.londogard.textgen.normalization

import kotlin.math.pow

class SoftMaxNormalization(override val temperature: Double) : Normalization {
    override fun normalize(probabilities: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
        val probs = probabilities.map { (_, score) -> score }.toDoubleArray()

        return probabilities.map { (index, _) -> index }.zip(normalize(probs).asList()) // TODO might be expensive to box
    }

    override fun normalize(probabilities: DoubleArray): DoubleArray {
        for (i in probabilities.indices) probabilities[i] = probabilities[i].pow(1 / (1 - temperature))
        val sum = probabilities.sum()
        for (i in probabilities.indices) probabilities[i] /= sum
        return probabilities
    }
}