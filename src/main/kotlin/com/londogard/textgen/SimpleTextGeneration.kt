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
    @JvmStatic
    fun main(args: Array<String>) {
        // generateText(3, 50, BeamSearch(3).search())

    }
    fun generateText(numReturnSequences: Int = 3,
                     numTokens: Int = 50,
                     temperature: Double = 0.7,
                     languageModel: LanguageModel,
                     normalization: Normalization = SoftMaxNormalization(temperature),
                     searchTechnique: Search = TopKSampleSearch(10),    // TODO searchTechnique should allow 'probModifier'
                     penalties: List<Penalty> = emptyList(),
                     smoothing: Smoothing = GreedyBackoff(normalization, penalties)//StupidBackoff(normalizer = normalization, penalties = penalties)
    ): List<String> {

        val history = mutableListOf(History(emptyList(), 0.0))
        val reverseDict = languageModel.getDictionary()

        return searchTechnique
            .search(numReturnSequences, numTokens, languageModel.n, languageModel, smoothing)
            .map { it.map(reverseDict::get).joinToString(" ") }
    }

    data class History(val history: List<Int>, val score: Double)
}

/**
 * TODO
 *  [ ] Allow seed in form text for text-gen
 *  [ ] Simplify Search
 *  [ ] Search should make unigramAsSequence and apply penalty...!
 *  [ ] Extract Smoothing Commons (at least internally on stupid/greddy backoff)
 *  [ ] Add NgramPenalty
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
