package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftMaxNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.predict.GreedyBackoff
import com.londogard.textgen.predict.Smoothing
import com.londogard.textgen.search.Search
import com.londogard.textgen.search.TopKSampleSearch
import kotlin.system.measureTimeMillis

// TODO might want to use float(s)!
object SimpleTextGeneration {
    fun generateText(
        numReturnSequences: Int = 3,
        numTokens: Int = 50,
        temperature: Double = 0.7,
        languageModel: LanguageModel,
        normalization: Normalization = SoftMaxNormalization(temperature),
        searchTechnique: Search = TopKSampleSearch(10),    // TODO searchTechnique should allow 'probModifier'
        penalties: List<Penalty> = emptyList(),
        smoothing: Smoothing = GreedyBackoff(normalization, penalties),
        seed: String = ""//StupidBackoff(normalizer = normalization, penalties = penalties)
    ): List<String> {

        val reverseDict = languageModel.getDictionary()
        val dict = languageModel.getReverseDictionary()
        val history = languageModel.tokenizer.split(seed).mapNotNull(dict::get)

        return searchTechnique
            .search(numReturnSequences, numTokens, languageModel.n, languageModel, smoothing, history)
            .map { it.map(reverseDict::get).joinToString(" ") }
    }
}

/**
 * TODO
 *  [X] Allow seed in form text for text-gen
 *  [ ] Simplify Search
 *  [X] Search should make unigramAsSequence and apply penalty...!
 *  [X] Extract Smoothing Commons (at least internally on stupid/greddy backoff)
 *  [X] Add NgramPenalty
 *  [ ] Add pre-trained models
 *  [X] Add support for \n, \t etc
 *  [-] Allow downloading of models (?)
 *  [X] Allow seed on random
 */

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val lm = LanguageModel(n = 7)

        javaClass.getResourceAsStream("/shakespeare.txt").bufferedReader().useLines { line ->
            lm.trainModel(listOf(line.joinToString(" \n ")))
        }
        lm.serialize("lm.cbor")

        println(SimpleTextGeneration.generateText(languageModel = lm, numReturnSequences = 5).joinToString("\n=====\n"))
    }
}
