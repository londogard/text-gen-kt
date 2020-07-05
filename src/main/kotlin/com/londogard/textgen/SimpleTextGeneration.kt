package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel2
import com.londogard.textgen.normalization.Normalization
import com.londogard.textgen.normalization.SoftMaxNormalization
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.predict.GreedyBackoff
import com.londogard.textgen.predict.Smoothing
import com.londogard.textgen.predict.StupidBackoff
import com.londogard.textgen.search.BeamSearch
import com.londogard.textgen.search.Search
import com.londogard.textgen.search.TopKSampleSearch

// TODO might want to use float(s)!
object SimpleTextGeneration {
    @JvmStatic
    fun main(args: Array<String>) {
        // generateText(3, 50, BeamSearch(3).search())

    }
    fun generateText(numReturnSequences: Int = 3,
                     numTokens: Int = 50,
                     temperature: Double = 0.7,
                     languageModel: LanguageModel2,
                     normalization: Normalization = SoftMaxNormalization(temperature),
                     searchTechnique: Search = TopKSampleSearch(10),    // TODO searchTechnique should allow 'probModifier'
                     penalties: List<Penalty> = emptyList(),
                     smoothing: Smoothing = GreedyBackoff(normalization, penalties)//StupidBackoff(normalizer = normalization, penalties = penalties)
    ): List<String> {

        val history = mutableListOf(History(emptyList(), 0.0))
        val lm = languageModel.getLanguageModel()
        val reverseDict = languageModel.getDictionary()

        return searchTechnique
            .search(numReturnSequences, numTokens, languageModel.n, lm, smoothing)
            .map { it.map(reverseDict::get).joinToString(" ") }
    }

    data class History(val history: List<Int>, val score: Double)
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val lm = LanguageModel2(n = 3)
        javaClass.getResourceAsStream("/shakespeare.txt").bufferedReader().useLines {
            val text = it.joinToString("\n")
            lm.trainModel(sequenceOf(text))
            val l = SimpleTextGeneration.generateText(languageModel = lm)
            println(l.joinToString("\n"))
        }
    }
}
