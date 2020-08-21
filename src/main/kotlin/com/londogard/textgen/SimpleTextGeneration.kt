package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftmaxNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.search.Search
import com.londogard.textgen.search.TopKSampleSearch
import com.londogard.textgen.smoothing.GreedyBackoff
import com.londogard.textgen.smoothing.Smoothing
import com.londogard.textgen.utils.PadUtil.padEnd
import com.londogard.textgen.utils.PadUtil.padStart
import com.londogard.textgen.utils.Sampling

/**
 * [[SimpleTextGeneration]] is the simple entry point to generate text.
 */
object SimpleTextGeneration {
    /**
     * @param languageModel: The Language Model to generate text from
     * @param seed: The seed to generate text from!
     * @param numTokens: Number of tokens to return per generation
     * @param temperature: The temperature to use [0,1) - lower is safer, higher is crazy.
     */
    fun simpleTextGeneration(
        languageModel: LanguageModel,
        seed: String = "",
        numTokens: Int = 250,
        temperature: Double = 0.2,
    ): List<String> {
        val reverseDict = languageModel.dictionary
        val dict = languageModel.getReverseDictionary()
        val history = languageModel.tokenizer.split(seed).mapNotNull(dict::get)
        val smoothing = GreedyBackoff(SoftmaxNormalization(temperature), emptyList())
        val numReturnSequences = 1

        return TopKSampleSearch(10)
            .search(numReturnSequences, numTokens, languageModel.n, languageModel, smoothing, history)
            .map { it.map(reverseDict::get).joinToString(languageModel.tokenizer.stringJoiner) }
            .map { it.replace(padStart, '\n').replace(padEnd, '\n') }
    }

    /**
     * @param languageModel: The Language Model to generate text from
     * @param numReturnSequences: Number of generations to return
     * @param numTokens: Number of tokens to return per generation
     * @param temperature: The temperature to use [0,1) - lower is safer, higher is crazy.
     * @param normalization: Normalization to use, Softmax & Londogard works best
     * @param searchTechnique: Which type of search to use, BeamSearch is very boring. TopK & TopP best!
     * @param penalties: What penalties to apply, currently implemented is only NgramPenalty
     * @param smoothing: Which smoothing technique to use, GreedyBackoff or StupidBackoff is the best!
     * @param seed: The seed to generate text from!
     * @param randomSeed: If you want the same results over and over!
     */
    fun generateText(
        languageModel: LanguageModel,
        numReturnSequences: Int = 1,
        numTokens: Int = 250,
        temperature: Double = 0.2,
        normalization: Normalization = SoftmaxNormalization(temperature),
        searchTechnique: Search = TopKSampleSearch(10),
        penalties: List<Penalty> = emptyList(),
        smoothing: Smoothing = GreedyBackoff(normalization, penalties),
        seed: String = "",
        randomSeed: Long? = null
    ): List<String> {
        if (randomSeed != null) Sampling.setSeed(randomSeed)

        val reverseDict = languageModel.dictionary
        val dict = languageModel.getReverseDictionary()
        val history = languageModel.tokenizer.split(seed).mapNotNull(dict::get)

        return searchTechnique
            .search(numReturnSequences, numTokens, languageModel.n, languageModel, smoothing, history)
            .map { it.map(reverseDict::get).joinToString(languageModel.tokenizer.stringJoiner) }
            .map { it.replace(padStart, '\n').replace(padEnd, '\n') }
    }
}