package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveData
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.predict.Smoothing.Companion.takeP

class NoSmoothing(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double =
        languageModel.getUnigramProbs().find { (key,_) -> key == token }?.second ?: 0.0

    override fun probabilitiesTopK(
        languageModel: LanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> = languageModel.retrieveData(history, penalties)
        .take(k)
        .let(normalizer::normalize)

    override fun probabilitiesTopP(
        languageModel: LanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> = languageModel.retrieveData(history, penalties)
        .takeP(p)
        .let(normalizer::normalize)
}