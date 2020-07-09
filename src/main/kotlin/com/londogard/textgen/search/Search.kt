package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.predict.Smoothing

// TODO add step func?
interface Search {
    fun search(numReturnSequences: Int,
               numTokens: Int,
               ngram: Int,
               languageModel: LanguageModel,
               smoothing: Smoothing,
               // earlyStopping = false <-- TODO
               seed: List<Int> = emptyList()
    ): List<List<Int>>
}