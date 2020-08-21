package com.londogard.textgen.smoothing

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveNgramData
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min

/**
 * [[GreedyBackoff]] applies a greedy approach which picks the top choices while backing off WITHOUT a penalty, unlike
 *  how [[StupidBackoff]] works which applies a penalty.
 */
class GreedyBackoff(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double {
        val range = min(languageModel.n, history.size) downTo 0
        for (i in range) {
            val tmpProb = languageModel
                .retrieveNgramData(history, emptyList(), i)
                .find { (key, _) -> key == token }?.second

            if (tmpProb != null) return tmpProb
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
        val fixedP = min(abs(p), 1.0)
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        var totalScore = 0.0
        val range = min(languageModel.n, history.size) downTo 0
        for (i in range) {
            languageModel
                .retrieveNgramData(history, penalties, i)
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < fixedP
                }
                .let(finalEntries::addAll)

            if (totalScore >= fixedP) return normalizer.normalize(finalEntries)
        }

        return normalizer.normalize(finalEntries)
    }
}