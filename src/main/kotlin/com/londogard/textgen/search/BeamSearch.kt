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
    ): List<List<Int>> =
        (0..numTokens)
            .fold(sequenceOf(seed to 0.0)) { acc, _ ->
                acc
                    .flatMap { (seq, score) ->
                        smoothing
                            .probabilitiesTopK(languageModel, seq, beams)
                            .map { (index, prob) -> seq + listOf(index) to score - ln(prob) }
                            .asSequence()
                    }
                    .sortedBy { (_, score) -> score }
                    .take(beams)
            }
            .take(numReturnSequences)
            .map { (seq, _) -> seq }
            .toList()
}