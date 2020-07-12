package com.londogard.textgen.search

import com.londogard.textgen.utils.Sampling.sample
import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing

class TopKSampleSearch(private val k: Int) : Search {
    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: LanguageModel,
        smoothing: Smoothing,
        seed: List<Int>
    ): List<List<Int>> {
        return (1..numReturnSequences)
            .fold(emptyList()) { returnSequence, _ ->
                returnSequence + listOf((1..numTokens).fold(seed) { history, _ ->
                    history + listOf(sample(smoothing.probabilitiesTopK(languageModel, history, k)))
                })
            }
    }
}