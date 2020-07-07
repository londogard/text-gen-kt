package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs

interface Smoothing {
    val normalizer: Normalization
    val penalties: List<Penalty>

    // JVM uses a "copy-by-value" meaning that we create a copy of the reference, so the object is never copied in functions.
    fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double
    fun probabilitiesTopK(
        languageModel: LanguageModel,
        history: List<Int>,
        k: Int = 10
    ): List<Pair<Int, Double>>

    fun probabilitiesTopP(
        languageModel: LanguageModel,
        history: List<Int>,
        p: Double = 0.9
    ): List<Pair<Int, Double>>

    companion object {
        internal fun List<Pair<Int, Double>>.takeP(p: Double = 1.0): List<Pair<Int, Double>> {
            var normP = kotlin.math.min(abs(p), 1.0)

            return this.takeWhile { (_, score) ->
                normP -= score
                (normP + score) >= 0
            }
        }
    }
}