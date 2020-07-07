package com.londogard.textgen.search

import com.londogard.textgen.utils.Sampling.sample
import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.predict.Smoothing

open class TopPSampleSearch(private val p: Double) : Search {
    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: LanguageModel,
        smoothing: Smoothing
    ): List<List<Int>> {
        return (1..numReturnSequences)
            .fold(emptyList()) { returnSequence, _ ->
                returnSequence + listOf((1..numTokens)
                    .fold(emptyList()) { history, _ ->
                        history + listOf(
                            sample(smoothing.probabilitiesTopP(languageModel, history.takeLast(ngram), p))
                        )
                    })
            }
    }
}