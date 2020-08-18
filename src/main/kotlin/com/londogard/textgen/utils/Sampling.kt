package com.londogard.textgen.utils

import kotlin.random.Random

object Sampling {
    private var random: Random = Random

    internal fun setSeed(seed: Long) {
        random = Random(seed)
    }

    fun sample(probs: List<Pair<Int, Double>>): Int {
        var rnd = random.nextDouble()
        probs.forEach { (i, score) ->
            rnd -= score
            if (rnd < 0) return i
        }
        return probs.last().first
    }

    // Top-K Sampling Fan et. al (2018) (https://arxiv.org/pdf/1805.04833.pdf)
    fun DoubleArray.topK(k: Int): List<Int> = sortedArrayDescending()
        .take(k) // TODO performance improvement
        .map { value -> this.indexOfFirst { it == value } }

    // Top-P Sampling (aka nucleus) Ari Holtzman et al. (2019) (https://arxiv.org/abs/1904.09751)
    fun DoubleArray.topP(p: Double = 0.92): List<Int> {
        val sorted = sortedArrayDescending()
        var pAcc = 0.0

        return sorted
            .takeWhile { d ->
                pAcc += d
                pAcc <= p
            }
            .map { value -> this.indexOfFirst { it == value } }
    }
}