package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.predict.Smoothing

// TODO add step func?
interface Search {
    // TODO add earlyStopping
    fun search(numReturnSequences: Int,
               numTokens: Int,
               ngram: Int,
               languageModel: LanguageModel,
               smoothing: Smoothing
    ): List<List<Int>>
}