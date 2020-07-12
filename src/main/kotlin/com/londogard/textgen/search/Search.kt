package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.Smoothing

interface Search {
    /**
     * Searches the Language Model for the numReturnSequences, with numTokens in each, best choices given configuration.
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