package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min

class GreedyBackoff(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: InternalLanguageModel, history: List<Int>, token: Int): Double {
        for (i in history.indices) {
            val tmpProb = languageModel[history.drop(i)]?.get(token)

            if (tmpProb != null) return tmpProb
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
                .let(finalEntries::addAll)

            if (finalEntries.size == k) return normalizer.normalize(finalEntries)
        }
        // TODO pre-sort the 0th array & make sure we only pick the number required from map! Expensive to run everything
        finalEntries.addAll(retrieveData(languageModel, emptyList()).take(k - finalEntries.size))
        return normalizer.normalize(finalEntries)
    }

    override fun probabilitiesTopP(
        languageModel: InternalLanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> {
        val fixedP = min(abs(p), 1.0)
        val finalEntries: MutableList<Pair<Int, Double>> = mutableListOf()
        var totalScore = 0.0
        for (i in history.indices) {
            retrieveData(languageModel, history.drop(i))
                .takeWhile { (_, score) ->
                    totalScore += score
                    (totalScore - score) < fixedP
                }
                .let(finalEntries::addAll)

            if (totalScore >= fixedP) return normalizer.normalize(finalEntries)
        }
        // TODO pre-sort the 0th array & make sure we only pick the number required from map! Expensive to run everything
        finalEntries.addAll(retrieveData(languageModel, emptyList()).takeWhile { (_, score) ->
            totalScore += score
            totalScore < fixedP
        })

        return normalizer.normalize(finalEntries)
    }
}