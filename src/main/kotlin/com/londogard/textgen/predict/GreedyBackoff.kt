package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveData
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min

class GreedyBackoff(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double {
        for (i in 1 until history.size) {
            val tmpProb = languageModel
                .retrieveData(history.drop(i), emptyList())
                .find { (key, _) -> key == token }?.second

            if (tmpProb != null) return tmpProb
        }

        return languageModel.getUnigramProbs().find { (key, _) -> key == token }?.second ?: 0.0
    }

    override fun probabilitiesTopK(
        languageModel: LanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> {
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        for (i in history.indices) {
            languageModel.retrieveData(history.drop(i), penalties)
                .take(k - finalEntries.size)
                .let(finalEntries::addAll)

            if (finalEntries.size == k) return normalizer.normalize(finalEntries)
        }
        languageModel
            .getUnigramProbs()
            .take(k - finalEntries.size)
            .let { entries -> penalties.fold(entries) { items, penalty -> penalty.penalize(items, history) } }
            .let(finalEntries::addAll)
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
        for (i in history.indices) {
            languageModel.retrieveData(history.drop(i), penalties)
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < fixedP
                }
                .let(finalEntries::addAll)

            if (totalScore >= fixedP) return normalizer.normalize(finalEntries)
        }
        languageModel
            .getUnigramProbs().asSequence()
            .map { prob -> penalties.fold(prob) { p, penalty -> penalty.penalize(listOf(p), history).first() } }
            .filterNot { (_, score) -> score <= 0 }
            .takeWhile { (_, score) ->
                totalScore += score
                (totalScore - score) < fixedP
            }.let(finalEntries::addAll)


        return normalizer.normalize(finalEntries)
    }
}