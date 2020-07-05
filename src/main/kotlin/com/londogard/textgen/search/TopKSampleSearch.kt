package com.londogard.textgen.search

import com.londogard.textgen.RandomUtil
import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.predict.Smoothing

class TopKSampleSearch(private val k: Int) : Search {
    private fun sample(probs: List<Pair<Int, Double>>): Int {
        probs.map { it.second }
        var rnd = RandomUtil.random.nextDouble()
        probs.forEach { (i, score) ->
            rnd -= score
            if (rnd < 0) return i
        }
        return probs.map { it.first }.last()
    }

    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: InternalLanguageModel,
        smoothing: Smoothing
    ): List<List<Int>> {
        return (1..numReturnSequences).fold(emptyList()) { returnSequence, _ ->
            returnSequence + listOf((1..numTokens).fold(emptyList()) { history, _ ->
                history + listOf(sample(smoothing.probabilitiesTopK(languageModel, history.takeLast(ngram), k)))
            })
        }
    }

    // Top-K Sampling Fan et. al (2018) (https://arxiv.org/pdf/1805.04833.pdf)
    fun DoubleArray.topK(k: Int): List<Int> = sortedArrayDescending()
        .take(k) // TODO performance improvement
        .map(this::indexOf)
}