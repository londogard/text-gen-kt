package com.londogard.textgen.languagemodels

import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.tokenizers.SimpleWordTokenizer
import com.londogard.textgen.tokenizers.Tokenizer
import com.londogard.textgen.utils.SerializerUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

typealias InternalLanguageModel = Map<List<Int>, List<Pair<Int, Double>>>
typealias InternalVocabulary = Map<Int, String>
typealias InternalReverseVocabulary = Map<String, Int>

/**
 * [[LanguageModel]] is the core data, this is the "mastermind" which we extract data from using different techniques.
 */
@Serializable
class LanguageModel(
    val tokenizer: Tokenizer,
    val dictionary: InternalVocabulary,
    val internalLanguageModel: InternalLanguageModel,
    val n: Int
) {
    fun getReverseDictionary(): InternalReverseVocabulary = dictionary.entries.associateBy({ it.value }) { it.key }

    @ExperimentalSerializationApi
    fun dump(absolutePath: String): Unit = SerializerUtil.encodeLanguageModel(absolutePath, this)

    companion object {
        @ExperimentalSerializationApi
        fun dump(languageModel: LanguageModel, absolutePath: String): Unit = languageModel.dump(absolutePath)

        @ExperimentalSerializationApi
        fun load(absolutePath: String): LanguageModel = SerializerUtil.decodeLanguageModelByAbsPath(absolutePath)

        fun fit(
            documents: List<String>,
            n: Int,
            tokenizer: Tokenizer = SimpleWordTokenizer(),
            keepMinFreq: Int = 0, // 0 = at least one occurrence, 1 = at least two occurrences, etc... Applied to all ngrams
            preprocessing: List<(String) -> String> = emptyList(),   // Example: listOf(TextNormalizer::normalize, String::toLowerCase)
        ): LanguageModel {
            val preprocessedDocs = documents
                .map { subDoc -> preprocessing.fold(subDoc) { acc, preprocessor -> preprocessor(acc) } }
            val tokens = preprocessedDocs.map(tokenizer::split)
            val reverseDict = tokens.flatten().toHashSet().let { uniqueTokens ->
                HashMap<String, Int>(uniqueTokens.size).apply { putAll(uniqueTokens.zip(uniqueTokens.indices)) }
            }
            val tokensList = tokens.map { t -> t.mapNotNull(reverseDict::get) }

            var previousMatch = true
            val ngramMap = (1..n)
                .fold(emptyMap<List<Int>, Int>()) { acc, ngram ->
                    when {
                        ngram <= 2 || previousMatch ->
                            acc + tokensList
                                .filter { docToken -> docToken.size >= ngram }
                                .flatMap { docToken ->
                                    (0..docToken.size - ngram)
                                        .mapNotNull { index ->  // Even though autobox applies to List<E> we save space here because of views.
                                            if (ngram <= 2 || acc.contains(docToken.subList(index, index + ngram - 1)))
                                                docToken.subList(index, index + ngram)
                                            else null
                                        }
                                }
                                .groupingBy { it }
                                .eachCount()
                                .filter { (_, value) -> value > keepMinFreq }
                                .also { if (it.isEmpty()) previousMatch = false }
                        else -> acc
                    }
                }
                .entries
                .groupBy({ (key, _) -> key.subList(0, key.size - 1) }) { (key, value) -> key.last() to value }
                .mapValues { (_, values) ->
                    val total = values.sumBy { (_, count) -> count }.toDouble()

                    values
                        .map { (key, value) -> key to (value / total) }
                        .sortedByDescending { (_, value) -> value }
                }

            return LanguageModel(tokenizer, reverseDict.entries.associateBy({ it.value }, { it.key }), ngramMap, n)
        }

        internal fun LanguageModel.retrieveNgramData(
            history: List<Int>, penalties: List<Penalty>, n: Int
        ): List<Pair<Int, Double>> = internalLanguageModel
            .getOrDefault(history.takeLast(n), emptyList())
            .let { entries -> penalties.fold(entries) { acc, penalty -> penalty.penalize(acc, history) } }
            .filterNot { (_, score) -> score <= 0 }
    }
}