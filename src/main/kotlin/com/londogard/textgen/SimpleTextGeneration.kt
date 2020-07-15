package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftMaxNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.smoothing.GreedyBackoff
import com.londogard.textgen.smoothing.Smoothing
import com.londogard.textgen.search.Search
import com.londogard.textgen.search.TopKSampleSearch
import com.londogard.textgen.utils.PadUtil
import smile.nlp.normalize
import java.io.File
import java.nio.file.Paths
import kotlin.streams.toList


object SimpleTextGeneration {
    fun generateText(
        numReturnSequences: Int = 3,
        numTokens: Int = 50,
        temperature: Double = 0.7,
        languageModel: LanguageModel,
        normalization: Normalization = SoftMaxNormalization(temperature),
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