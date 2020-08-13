package com.londogard.textgen.search

import com.londogard.textgen.utils.Sampling.sample
import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing

/** Uses Top-P Sampling (nucleus) [Ari Holtzman et al. (2019): https://arxiv.org/abs/1904.09751] */
open class TopPSampleSearch(private val p: Double) : Search {
    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: LanguageModel,
        smoothing: Smoothing,
        seed: List<Int>
    ): List<List<Int>> =
        (1..numReturnSequences)
            .fold(emptyList()) { returnSequence, _ ->
                val generatedText = (1..numTokens)
                    .fold(seed) { history, _ ->
                        val sample = sample(smoothing.probabilitiesTopP(languageModel, history, p))
                        history.plusElement(sample)
                    }
                returnSequence.plusElement(generatedText)
            }
}