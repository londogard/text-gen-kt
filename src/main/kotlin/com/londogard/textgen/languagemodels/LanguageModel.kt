package com.londogard.textgen.languagemodels

import com.londogard.textgen.Config
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.tokenizers.SimpleExtensibleTokenizer
import com.londogard.textgen.tokenizers.Tokenizer
import com.londogard.textgen.utils.PadUtil.padStartEnd
import com.londogard.textgen.utils.Sampling
import com.londogard.textgen.utils.SerializerUtil
import java.io.File
import java.util.regex.Pattern
import kotlin.system.measureTimeMillis

typealias InternalLanguageModel = Map<List<Int>, List<Pair<Int, Double>>>
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
    val n: Int = config.n

    fun getReverseDictionary(): InternalReverseVocabulary = dictionary.entries.associateBy({ it.value }) { it.key }

    fun getLanguageModel(): InternalLanguageModel = internalLanguageModel
    fun getConfig(): Config = Config(n, getLanguageModel(), dictionary)
    fun dump(absolutePath: String): Unit = SerializerUtil.serializeConfig(absolutePath, getConfig())

    companion object {
        fun dump(config: Config, absolutePath: String): Unit = SerializerUtil.serializeConfig(absolutePath, config)
        fun load(
            absolutePath: String,
            tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" ")),
            seed: Long? = null
        ): LanguageModel {
            val config = SerializerUtil.deserializeConfigByAbsolutePath(absolutePath)

            return LanguageModel(tokenizer, config, seed)
        }

        fun fit(
            documents: List<String>,
            n: Int,
            tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" ")),
            keepMinFreq: Int = 0 // 0 = at least one occurrence, 1 = at least two occurrences, etc... Applied to all ngrams
        ): LanguageModel {
            val tokens = documents.map { tokenizer.split(it).asList() }
            val dictionary = tokens.flatten().toHashSet().let { uniqueTokens ->
                HashMap<Int, String>(uniqueTokens.size).apply { putAll(uniqueTokens.indices.zip(uniqueTokens)) }
            }
            val reverseDict = dictionary.entries.associateBy({ it.value }) { it.key }
            val tokensInt = tokens.map { docTokens -> docTokens.mapNotNull(reverseDict::get) }

            val ngramMap = tokensInt
                .flatMap { docTokens ->
                    docTokens
                        .windowed(n, partialWindows = true) { window ->
                            when (window.size) {
                                n -> (1..n).map { i -> window.take(i) }
                                else -> listOf(window)
                            }
                        }
                        .flatten()
                }
                .groupBy({ it.subList(0, it.size - 1) }) { it.last() }
                .mapValues { (_, value) ->
                    val totalSize = value.size.toDouble()

                    value
                        .groupingBy { it }
                        .eachCount()
                        .filter { (_, occurrences) -> occurrences > keepMinFreq }
                        .map { (key, value) -> key to (value / totalSize) }
                        .sortedByDescending { it.second }
                }
                .filter { (_, submap) -> submap.isNotEmpty() }

            return LanguageModel(tokenizer, Config(n, ngramMap, dictionary))
        }

        internal fun LanguageModel.retrieveNgramData(
            history: List<Int>, penalties: List<Penalty>, n: Int
        ): List<Pair<Int, Double>> = internalLanguageModel
            .getOrDefault(history.takeLast(n), emptyList())
            .let { entries -> penalties.fold(entries) { acc, penalty -> penalty.penalize(acc, history) } }
            .filterNot { (_, score) -> score <= 0 }

        @JvmStatic
        fun main(args: Array<String>) {
            val text = File("/home/londet/git/text-gen-kt/files/cardsagainst_white.txt").readLines()
            val data = if (false) listOf(text.joinToString(" \n ")) else text.map { it.padStartEnd(3) }
            LanguageModel.fit(data, 3).dump("/home/londet/git/text-gen-kt/files/models/cardsagainst_white_3.cbor")
            LanguageModel.fit(data, 3, keepMinFreq = 1)
                .dump("/home/londet/git/text-gen-kt/files/models/cardsagainst_white_3_mini.cbor")
            LanguageModel.fit(data, 5, keepMinFreq = 1)
                .dump("/home/londet/git/text-gen-kt/files/models/cardsagainst_white_5.cbor")
            LanguageModel.fit(data, 7, keepMinFreq = 1)
                .dump("/home/londet/git/text-gen-kt/files/models/cardsagainst_white_7.cbor")
        }
    }
}