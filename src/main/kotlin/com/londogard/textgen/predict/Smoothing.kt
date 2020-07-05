package com.londogard.textgen.predict

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.penalties.Penalty

// TODO add NgramPenalty to retrieval of probabilities
// TODO add Normalizer
// TODO curry func?
interface Smoothing {
    val normalizer: Normalization
    val penalties: List<Penalty>

    fun predict(languageModel: InternalLanguageModel, history: List<Int>, token: Int): Double
    fun probabilitiesTopK(languageModel: InternalLanguageModel, history: List<Int>, k: Int = 10): List<Pair<Int, Double>>
    fun probabilitiesTopP(languageModel: InternalLanguageModel, history: List<Int>, p: Double = 0.9): List<Pair<Int, Double>>

    /** Not to be used */
    fun retrieveData(languageModel: InternalLanguageModel, history: List<Int>): List<Pair<Int, Double>> = languageModel[history]
        ?.entries
        ?.map { it.toPair() }
        ?.let { entries -> penalties.fold(entries) { acc, penalty -> penalty.penalize(acc, history) } }
        ?.filterNot { (_, score) -> score <= 0 }
        ?.sortedByDescending { (_, score) -> score } ?: emptyList()
}