package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing

/**
 * [[Search]] is the way that the [[LanguageModel]] is searched, different types of search applies different techniques.
 */
interface Search {
    /**
     * Searches the [[LanguageModel]] for the [numReturnSequences], with [numTokens] in each, best choices given configuration.
     */
    fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: LanguageModel,
        smoothing: Smoothing,
        // earlyStopping = false <-- TODO
        seed: List<Int> = emptyList()
    ): List<List<Int>>
}