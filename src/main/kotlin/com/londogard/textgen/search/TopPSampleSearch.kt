package com.londogard.textgen.search

import com.londogard.textgen.RandomUtil
import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.predict.Smoothing

open class TopPSampleSearch(private val p: Double) : Search {
    private fun sample(probs: List<Pair<Int, Double>>): Int {
        var rnd = RandomUtil.random.nextDouble()

        probs.forEach { (i, score) ->
            rnd -= score
            if (rnd <= 0) return i
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
        return (1..numReturnSequences)
            .fold(emptyList()) { returnSequence, _ ->
                returnSequence + listOf((1..numTokens)
                    .fold(emptyList()) { history, _ ->
                        history + listOf(
                            sample(
                                smoothing.probabilitiesTopP(
                                    languageModel,
                                    history.takeLast(ngram),
                                    p
                                )
                            )
                        )
                    })
            }
    }
}