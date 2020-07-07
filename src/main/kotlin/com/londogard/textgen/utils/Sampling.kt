package com.londogard.textgen.utils

import smile.math.Random


object Sampling {
    private val random = Random()
    internal fun setSeed(seed: Long): Unit = random.setSeed(seed)

    fun sample(probs: List<Pair<Int, Double>>): Int {
        probs.map { it.second }
        var rnd = random.nextDouble()
        probs.forEach { (i, score) ->
            rnd -= score
            if (rnd < 0) return i
        }
        return probs.map { it.first }.last()
    }

    // Top-K Sampling Fan et. al (2018) (https://arxiv.org/pdf/1805.04833.pdf)
    fun DoubleArray.topK(k: Int): List<Int> = sortedArrayDescending()
        .take(k) // TODO performance improvement
        .map(this::indexOf)
}