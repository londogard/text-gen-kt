package com.londogard.textgen.search

import com.londogard.textgen.utils.Sampling.sample
import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing

/** Uses Top-K Sampling [Fan et. al (2018): https://arxiv.org/pdf/1805.04833.pdf] */
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
                val generatedText = (1..numTokens).fold(seed) { history, _ ->
                    val sample = sample(smoothing.probabilitiesTopK(languageModel, history, k))
                    history.plusElement(sample)
                }
                returnSequence.plusElement(generatedText)
            }
    }
}