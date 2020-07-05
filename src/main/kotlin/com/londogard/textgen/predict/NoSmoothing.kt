package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import kotlin.math.abs
import kotlin.math.min

class NoSmoothing(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: InternalLanguageModel, history: List<Int>, token: Int): Double =
        languageModel[history]?.get(token) ?: 0.0

    override fun probabilitiesTopK(
        languageModel: InternalLanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> = retrieveData(languageModel, history)
        .take(k)
        .let(normalizer::normalize)

    override fun probabilitiesTopP(
        languageModel: InternalLanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> {
        var normP = min(abs(p), 1.0)
        return retrieveData(languageModel, history)
            .takeWhile { (_, score) ->
                normP -= score
                (normP + score) >= 0
            }
            .let(normalizer::normalize)
    }
}