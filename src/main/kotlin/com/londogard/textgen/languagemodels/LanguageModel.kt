package com.londogard.textgen.languagemodels

import com.londogard.textgen.Config
import com.londogard.textgen.penalties.Penalty
import com.londogard.textgen.tokenizers.SimpleExtensibleTokenizer
import com.londogard.textgen.utils.Sampling
import com.londogard.textgen.utils.SerializerUtil
import smile.nlp.tokenizer.Tokenizer
import java.util.regex.Pattern
import kotlin.system.measureTimeMillis

private typealias InternalMutableLanguageModel = HashMap<List<Int>, List<Pair<Int, Double>>>
typealias InternalLanguageModel = Map<List<Int>, List<Pair<Int, Double>>>
typealias InternalVocabulary = Map<Int, String>

// TODO add int -> short & double -> float possibilities to save space!
// TODO cache unigrams in map (top 500 + future uses)
class LanguageModel(
    val tokenizer: Tokenizer = SimpleExtensibleTokenizer(whitespace = Pattern.compile(" ")),
    val n: Int,
    seed: Long? = null
) {
    private val dictionary: HashMap<Int, String> = hashMapOf()
    private val internalLanguageModel: InternalMutableLanguageModel = hashMapOf()
    private val sortedUnigramProbs: MutableList<Pair<Int, Double>> = mutableListOf()

    init {
        if (seed != null) Sampling.setSeed(seed)
    }

    fun initByConfig(config: Config) {
        dictionary.clear()
        dictionary.putAll(config.vocab)
        internalLanguageModel.clear()
        internalLanguageModel.putAll(config.languageModel)
        sortedUnigramProbs.addAll(config.unigram)
    }

    fun getDictionary(): Map<Int, String> = dictionary
    fun getReverseDictionary(): Map<String, Int> = dictionary.entries.associateBy({ it.value }) { it.key }

    fun getUnigramProbs(): List<Pair<Int, Double>> = sortedUnigramProbs
    fun getLanguageModel(): InternalLanguageModel = internalLanguageModel
    fun getConfig(): Config = Config(n, getLanguageModel(), getDictionary(), sortedUnigramProbs)
    fun serialize(absolutePath: String): Unit = SerializerUtil.serializeConfig(absolutePath, getConfig())
    fun deserialize(absolutePath: String): Unit =
        initByConfig(SerializerUtil.deserializeConfigByAbsolutePath(absolutePath))

    fun trainModel(documents: List<String>) {
        dictionary.clear()
        internalLanguageModel.clear()
        val tokens = documents.flatMap { tokenizer.split(it).asList() } // perhaps just make it a var?
        dictionary.putAll(tokens.toHashSet().let { it.indices.zip(it) })
        val reverseDict = getReverseDictionary()
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
            .groupBy({ it.subList(0, it.size - 1) }, { it.last() })
            .mapValues { (_, value) ->
                val totalSize = value.size.toDouble()

                value
                    .groupingBy { it }
                    .eachCount().entries
                    .map { (key, value) -> key to (value / totalSize) }
                    .sortedByDescending { it.second }
            }
        sortedUnigramProbs.addAll(unigrams)
        internalLanguageModel.putAll(ngramMap)
    }

    internal companion object {
        fun LanguageModel.retrieveData(
            history: List<Int>,
            penalties: List<Penalty>
        ): List<Pair<Int, Double>> = when {
            history.isNotEmpty() -> this.internalLanguageModel.getOrDefault(history, emptyList())
                .let { entries -> penalties.fold(entries) { acc, penalty -> penalty.penalize(acc, history) } }
                .filterNot { (_, score) -> score <= 0 }
            else -> this.sortedUnigramProbs
        }
    }
}

object B {
    @JvmStatic
    fun main(args: Array<String>) {
        val text = javaClass.getResourceAsStream("/shakespeare.txt").bufferedReader().readText()
        val lm = LanguageModel(n = 3)
        println(
            measureTimeMillis {
                (1..10).forEach { lm.trainModel(listOf(text)) }
            } / 10.0
        )
    }
}