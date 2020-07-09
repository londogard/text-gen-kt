package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveData
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftMaxNormalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class StupidBackoff(
    var alpha: Double = 0.4,
    override val normalizer: Normalization = SoftMaxNormalization(0.7),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double {
        for (i in 1 until history.size) {
            val tmpProb = languageModel
                .retrieveData(history.drop(i), emptyList())
                .find { (key, _) -> key == token }?.second

            if (tmpProb != null) return (tmpProb * alpha.pow(i.toDouble()))
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
            languageModel
                .retrieveData(history.drop(i), penalties)
                .take(k - finalEntries.size)
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .let(finalEntries::addAll)

            if (finalEntries.size == k) return normalizer.normalize(finalEntries)
        }
        languageModel
            .getUnigramProbs()
            .take(k - finalEntries.size)
            .map { (index, score) -> index to score * alpha.pow(history.size) }
            .let(finalEntries::addAll)

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
        for (i in history.indices) {
            languageModel.retrieveData(history.drop(i), penalties)
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < normP
                }
                .let(finalEntries::addAll)

            if (totalScore >= normP) return normalizer.normalize(finalEntries)
        }
        // TODO apply penalties both in this & stupidBackoff!
        languageModel.getUnigramProbs()
            .asSequence()
            .map { prob -> penalties.fold(prob) { p, penalty -> penalty.penalize(listOf(p), history).first() } }
            .filterNot { (_, score) -> score <= 0 }
            .map { (index, score) -> index to score * alpha.pow(history.size) }
            .takeWhile { (_, score) ->
                totalScore += score
                (totalScore - score) < normP
            }
            .let(finalEntries::addAll)

        return normalizer.normalize(finalEntries)
    }
}