package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.normalization.LondogardNormalization
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftmaxNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.smoothing.GreedyBackoff
import com.londogard.textgen.smoothing.Smoothing
import com.londogard.textgen.search.Search
import com.londogard.textgen.search.TopKSampleSearch
import com.londogard.textgen.utils.PadUtil


object SimpleTextGeneration {
    fun generateText(
        languageModel: LanguageModel,
        numReturnSequences: Int = 3,
        numTokens: Int = 50,
        temperature: Double = 0.7,
        normalization: Normalization = SoftmaxNormalization(temperature),
        searchTechnique: Search = TopKSampleSearch(10),
        penalties: List<Penalty> = emptyList(),
        smoothing: Smoothing = GreedyBackoff(normalization, penalties),
        seed: String = ""
    ): List<String> {
        val reverseDict = languageModel.dictionary
        val dict = languageModel.getReverseDictionary()
        val history = languageModel.tokenizer.split(seed).mapNotNull(dict::get)

        return searchTechnique
            .search(numReturnSequences, numTokens, languageModel.n, languageModel, smoothing, history)
            .map { it.map(reverseDict::get).joinToString(" ") }
    }
}