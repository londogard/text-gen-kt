package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.predict.Smoothing

// TODO add step func?
interface Search {
    // TODO add earlyStopping
    fun search(numReturnSequences: Int,
               numTokens: Int,
               ngram: Int,
               languageModel: InternalLanguageModel,
               smoothing: Smoothing
    ): List<List<Int>>
}