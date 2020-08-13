package com.londogard.textgen.smoothing

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveNgramData
import com.londogard.textgen.normalization.LondogardNormalization
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class StupidBackoff(
    var alpha: Double = 0.4,
    override val normalizer: Normalization = LondogardNormalization(0.7),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double {
        val range = min(languageModel.n, history.size) downTo 0
        for (i in range) {
            val tmpProb = languageModel
                .retrieveNgramData(history, emptyList(), i)
                .find { (key, _) -> key == token }?.second

            if (tmpProb != null) return (tmpProb * alpha.pow(i.toDouble()))
        }
        return languageModel.internalLanguageModel[emptyList()]?.find { (key, _) -> key == token }?.second ?: 0.0
    }

    override fun probabilitiesTopK(
        languageModel: LanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> {
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        val range = min(languageModel.n, history.size) downTo 0
        for (i in range) {
            languageModel
                .retrieveNgramData(history, penalties, i)
                .take(k - finalEntries.size)
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .let(finalEntries::addAll)

            if (finalEntries.size == k) return normalizer.normalize(finalEntries)
        }

        return normalizer.normalize(finalEntries)
    }

    override fun probabilitiesTopP(
        languageModel: LanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> {
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        val normP = min(abs(p), 1.0)
        var totalScore = 0.0
        val range = min(languageModel.n, history.size) downTo 0
        for (i in range) {
            languageModel
                .retrieveNgramData(history, penalties, i)
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < normP
                }
                .let(finalEntries::addAll)

            if (totalScore >= normP) return normalizer.normalize(finalEntries)
        }

        return normalizer.normalize(finalEntries)
    }
}