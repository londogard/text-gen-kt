package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.InternalLanguageModel
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
    override fun predict(languageModel: InternalLanguageModel, history: List<Int>, token: Int): Double {
        for (i in history.indices) {
            val tmpProb = languageModel[history.drop(i)]?.get(token)

            if (tmpProb != null) return (tmpProb * alpha.pow(i.toDouble()))
        }
        return 0.0
    }

    override fun probabilitiesTopK(
        languageModel: InternalLanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> {
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        for (i in history.indices) {
            retrieveData(languageModel, history.drop(i))
                .take(k - finalEntries.size)
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .let(finalEntries::addAll)

            if (finalEntries.size == k) return normalizer.normalize(finalEntries)
        }

        finalEntries.addAll(retrieveData(languageModel, emptyList())
            .take(k - finalEntries.size)
            .map { (index, score) -> index to score * alpha.pow(history.size) })
        return normalizer.normalize(finalEntries)
    }

    override fun probabilitiesTopP(
        languageModel: InternalLanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> {
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        val normP = min(abs(p), 1.0)
        var totalScore = 0.0
        for (i in history.indices) {
            retrieveData(languageModel, history.drop(i))
                .map { (index, score) -> index to score * alpha.pow(i.toDouble()) }
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < normP
                }
                .let(finalEntries::addAll)

            if (totalScore >= normP) return normalizer.normalize(finalEntries)
        }
        finalEntries.addAll(retrieveData(languageModel, emptyList()).takeWhile { (_, score) ->
            totalScore += score
            totalScore < normP
        }.map { (index, score) -> index to score * alpha.pow(history.size) })
        // TODO pre-sort the 0th array; return score>=P by using 0th array!
        return normalizer.normalize(finalEntries)
    }
}