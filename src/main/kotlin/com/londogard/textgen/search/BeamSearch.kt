package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing
import kotlin.math.ln

/** Perhaps a step function would make sense? */
open class BeamSearch(private val beams: Int) : Search {
    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: LanguageModel,
        smoothing: Smoothing,
        seed: List<Int>
    ): List<List<Int>> {
        var sequences = listOf(seed to 0.0)
        (0..numTokens).forEach { _ ->
            val allCandidates = mutableListOf<Pair<List<Int>, Double>>()
            for (i in sequences.indices) {
                val (seq, score) = sequences[i]
                val probs = smoothing
                    .probabilitiesTopK(languageModel, seq, beams)
                    .map { (index, prob) -> seq + listOf(index) to score - ln(prob) }

                allCandidates.addAll(probs)
            }
            allCandidates.sortBy { it.second }
            sequences = allCandidates.take(beams)
        }
        return sequences.take(numReturnSequences).map { (sequence, _) -> sequence }
    }
}