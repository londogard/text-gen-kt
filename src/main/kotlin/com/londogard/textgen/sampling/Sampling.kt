package com.londogard.textgen.sampling

import com.londogard.textgen.RandomUtil
import kotlin.math.pow

interface Sampling {
    /** The higher temperature, the sharper curve */
    fun DoubleArray.softMaxDistribution(temperature: Double = 0.7): DoubleArray {
        for (i in indices) this[i] = this[i].pow(1 / (1 - temperature))
        val sum = sum()
        for (i in indices) this[i] /= sum
        return this
    }

    fun DoubleArray.sampleGreedy(): Int = max()?.let(this::indexOf) ?: 0

    fun DoubleArray.sample(): Int {
        var rnd = RandomUtil.random.nextDouble()
        for (i in indices) {
            if (rnd < 0) return i
            else rnd -= this[i]
        }
        return lastIndex
    }

    // Top-K Sampling Fan et. al (2018) (https://arxiv.org/pdf/1805.04833.pdf)
    fun DoubleArray.topK(k: Int): List<Int> = sortedArrayDescending()
        .take(k) // TODO performance improvement
        .map(this::indexOf)

    // Top-P Sampling, also called nucleus - Ari Holtzman et al. (2019) (https://arxiv.org/abs/1904.09751)
    fun DoubleArray.topP(p: Double = 0.92): List<Int> {
        val sorted = sortedArrayDescending()
        var pAcc = 0.0

        return sorted
            .takeWhile { d ->
                pAcc += d
                pAcc <= p
            }
            .map(this::indexOf)
    }

    fun softmaxSample(probabilities: DoubleArray, temperature: Double): DoubleArray {
        for (i in probabilities.indices) probabilities[i] = probabilities[i].pow(1 / temperature)
        val sum = probabilities.sum()
        for (i in probabilities.indices) probabilities[i] /= sum
        return probabilities
    }
}