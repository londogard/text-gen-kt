package com.londogard.textgen.languagemodels

import com.londogard.textgen.Config
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.tokenizers.SimpleExtensibleTokenizer
import com.londogard.textgen.tokenizers.Tokenizer
import com.londogard.textgen.utils.Sampling
import com.londogard.textgen.utils.SerializerUtil
import java.util.regex.Pattern

typealias InternalLanguageModel = Map<List<Int>, List<Pair<Int, Double>>>
typealias InternalUnigramModel = List<Pair<Int, Double>>
typealias InternalVocabulary = Map<Int, String>
typealias InternalReverseVocabulary = Map<String, Int>

// TODO add int -> short & double -> float possibilities to save space!
// TODO cache unigrams in map (top 500 + future uses)
class LanguageModel(
    val tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" ")),
    config: Config,
    seed: Long? = null
) {
    init {
        if (seed != null) Sampling.setSeed(seed)
    }

    val dictionary: InternalVocabulary = config.vocab
    val internalLanguageModel: InternalLanguageModel = config.languageModel
    val sortedUnigramProbabilities: InternalUnigramModel = config.unigram
    val n: Int = config.n

    fun getReverseDictionary(): InternalReverseVocabulary = dictionary.entries.associateBy({ it.value }) { it.key }

    fun getLanguageModel(): InternalLanguageModel = internalLanguageModel
    fun getConfig(): Config = Config(n, getLanguageModel(), dictionary, sortedUnigramProbabilities)
    fun serialize(absolutePath: String): Unit = SerializerUtil.serializeConfig(absolutePath, getConfig())

    companion object {
        fun loadPretrainedModel(
            absolutePath: String,
            tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" ")),
            seed: Long? = null
        ): LanguageModel {
            val config = SerializerUtil.deserializeConfigByAbsolutePath(absolutePath)

            return LanguageModel(tokenizer, config, seed)
        }

        fun trainModel(
            documents: List<String>,
            n: Int,
            tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" "))
        ): LanguageModel {
            val tokens = documents.flatMap { tokenizer.split(it).asList() }
            val dictionary = tokens.toHashSet().let { uniqueTokens ->
                HashMap<Int, String>(uniqueTokens.size).apply { putAll(uniqueTokens.indices.zip(uniqueTokens)) }
            }
            val reverseDict = dictionary.entries.associateBy({ it.value }) { it.key }
            val tokensInt = tokens.mapNotNull(reverseDict::get)
            val numTokens = tokensInt.size.toDouble()
            val unigrams = tokensInt
                .groupingBy { it }
                .eachCount().entries
                .map { (key, value) -> key to (value / numTokens) }
                .sortedByDescending { it.second }

            val ngramMap = tokensInt
                .windowed(n) { window -> (2..n).map { i -> window.take(i) } }
                .flatten()
                .groupBy({ it.subList(0, it.size - 1) }) { it.last() }
                .mapValues { (_, value) ->
                    val totalSize = value.size.toDouble()

                    value
                        .groupingBy { it }
                        .eachCount()
                        .map { (key, value) -> key to (value / totalSize) }
                        .sortedByDescending { it.second }
                }

            return LanguageModel(tokenizer, Config(n, ngramMap, dictionary, unigrams))
        }

        internal fun LanguageModel.retrieveNgramData(
            history: List<Int>,
            penalties: List<Penalty>,
            n: Int
        ): List<Pair<Int, Double>> = when {
            history.isNotEmpty() -> this.internalLanguageModel
                .getOrDefault(history.takeLast(n), emptyList())
                .let { entries -> penalties.fold(entries) { acc, penalty -> penalty.penalize(acc, history) } }
                .filterNot { (_, score) -> score <= 0 }
            else -> this.sortedUnigramProbabilities
        }

        internal fun LanguageModel.retrieveUnigramData(
            history: List<Int>,
            penalties: List<Penalty>
        ): Sequence<Pair<Int, Double>> =
            this.sortedUnigramProbabilities.asSequence()
                .map { prob -> penalties.fold(prob) { p, penalty -> penalty.penalize(listOf(p), history).first() } }
                .filterNot { (_, score) -> score <= 0 }
    }
}