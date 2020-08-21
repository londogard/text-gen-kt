package com.londogard.textgen.smoothing

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.languagemodels.LanguageModel.Companion.retrieveNgramData
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.smoothing.Smoothing.Companion.takeP

/** [[NoSmoothing]] is a search technique which returns empty if we don't have a word for the exact history given. */
class NoSmoothing(
    override val normalizer: Normalization = SimpleNormalization(),
    override val penalties: List<Penalty> = emptyList()
) : Smoothing {
    override fun predict(languageModel: LanguageModel, history: List<Int>, token: Int): Double =
        languageModel.internalLanguageModel[emptyList()]?.find { (key,_) -> key == token }?.second ?: 0.0

    override fun probabilitiesTopK(
        languageModel: LanguageModel,
        history: List<Int>,
        k: Int
    ): List<Pair<Int, Double>> = languageModel.retrieveNgramData(history, penalties, languageModel.n)
        .take(k)
        .let(normalizer::normalize)

    override fun probabilitiesTopP(
        languageModel: LanguageModel,
        history: List<Int>,
        p: Double
    ): List<Pair<Int, Double>> = languageModel.retrieveNgramData(history, penalties, languageModel.n)
        .takeP(p)
        .let(normalizer::normalize)
}