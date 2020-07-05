package com.londogard.textgen.search

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.predict.Smoothing
import kotlin.math.ln

/** Perhaps a step function would make sense? */
open class BeamSearch(private val beams: Int) : Search {
    override fun search(
        numReturnSequences: Int,
        numTokens: Int,
        ngram: Int,
        languageModel: InternalLanguageModel,
        smoothing: Smoothing
    ): List<List<Int>> {
        var sequences = listOf(emptyList<Int>() to 0.0)
        (0..numTokens).forEach { _ ->
            val allCandidates = mutableListOf<Pair<List<Int>, Double>>()
            for (i in sequences.indices) {
                val (seq, score) = sequences[i]
                val probs = smoothing.probabilitiesTopK(languageModel, seq.takeLast(ngram), beams)
                    .map { (index, prob) -> seq + listOf(index) to score - ln(prob) }

                allCandidates.addAll(probs)
            }
            allCandidates.sortBy { it.second }
            sequences = allCandidates.take(beams)
        }
        return sequences.take(numReturnSequences).map { (sequence, _) -> sequence }
    }

    fun InternalLanguageModel.search(
        beams: Int = 1,
        maxTokens: Int = 50,
        numBeamsReturn: Int = beams,
        ngram: Int = 3,
        getProbs: InternalLanguageModel.(List<Int>, Int) -> List<Pair<Int, Double>>
        // TODO support earlyStopping=true - end when all beams reached <EOS>
    ): List<List<Int>> {
        var sequences = listOf(emptyList<Int>() to 0.0)
        (0..maxTokens).forEach { _ ->
            val allCandidates = mutableListOf<Pair<List<Int>, Double>>()
            for (i in sequences.indices) {
                val (seq, score) = sequences[i]
                val probs = this.getProbs(seq.takeLast(ngram), beams)
                    .map { (index, prob) -> seq + listOf(index) to score - ln(prob) }
                allCandidates.addAll(probs)
            }
            allCandidates.sortBy { it.second }
            sequences = allCandidates.take(beams)
        }
        return sequences.take(numBeamsReturn).map { (sequence, _) -> sequence }
    }

    fun searchLazily(
        dataGeneration: (List<Int>) -> Float,
        beams: Int = 1,
        vocabSize: Int,
        // earlyStopping: Boolean = true, TODO add earlyStoppingSupport!
        maxTokens: Int = 50,
        numBeamsReturn: Int = beams
    ): List<Pair<List<Int>, Float>> {
        var sequences = listOf(listOf<Int>() to 0f)
        (0..maxTokens).forEach { _ ->
            val allCadidates = mutableListOf<Pair<List<Int>, Float>>()
            for (i in sequences.indices) {
                val (seq, score) = sequences[i]
                for (j in 0..vocabSize) {
                    val candidate = seq + listOf(j) to score - ln(dataGeneration(seq))
                    allCadidates.add(candidate)
                }
            }
            allCadidates.sortBy { it.second }
            sequences = allCadidates.take(beams)
        }
        return sequences.take(numBeamsReturn)
    }

    fun search(data: List<List<Double>>, beams: Int = 1): List<Pair<List<Int>, Float>> {
        var sequences = listOf(listOf<Int>() to 0f) // format
        data.forEach { row ->
            val allCadidates = mutableListOf<Pair<List<Int>, Float>>()
            for (i in sequences.indices) {
                val (seq, score) = sequences[i]
                for (j in row.indices) {
                    val candidate = seq + listOf(j) to score - ln(row[j].toFloat())
                    allCadidates.add(candidate)
                }
            }
            allCadidates.sortBy { it.second }
            sequences = allCadidates.take(beams)
        }
        return sequences
    }
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val data = listOf(
            listOf(0.1, 0.2, 0.3, 0.4, 0.5),
            listOf(0.5, 0.4, 0.3, 0.2, 0.1),
            listOf(0.1, 0.2, 0.3, 0.4, 0.5),
            listOf(0.5, 0.4, 0.3, 0.2, 0.1),
            listOf(0.1, 0.2, 0.3, 0.4, 0.5),
            listOf(0.5, 0.4, 0.3, 0.2, 0.1),
            listOf(0.1, 0.2, 0.3, 0.4, 0.5),
            listOf(0.5, 0.4, 0.3, 0.2, 0.1),
            listOf(0.1, 0.2, 0.3, 0.4, 0.5),
            listOf(0.5, 0.4, 0.3, 0.2, 0.1)
        )
        BeamSearch(3).search(data, 3).forEach { println(it) }
        /** Should be
        ([4, 0, 4, 0, 4, 0, 4, 0, 4, 0], 6.931471805599453)
        ([4, 0, 4, 0, 4, 0, 4, 0, 4, 1], 7.154615356913663)
        ([4, 0, 4, 0, 4, 0, 4, 0, 3, 0], 7.154615356913663)
         */
    }
}