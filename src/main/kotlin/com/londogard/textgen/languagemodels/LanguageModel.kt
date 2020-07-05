package com.londogard.textgen.languagemodels

import com.londogard.textgen.NGram
import smile.nlp.tokenizer.SimpleTokenizer
import smile.nlp.tokenizer.Tokenizer
import kotlin.system.measureTimeMillis

internal typealias InternalMutableLanguageModel = HashMap<List<Int>, MutableMap<Int, Double>>
typealias InternalLanguageModel = Map<List<Int>, Map<Int, Double>>

class LanguageModel2(private val tokenizer: Tokenizer = SimpleTokenizer(), val n: Int) {
    private val dictionary: HashMap<Int, String> = hashMapOf()
    private val internalLanguageModel: InternalMutableLanguageModel = hashMapOf()
    private val defaultValue = 0.0

    fun getDictionary(): Map<Int, String> = dictionary
    fun getReverseDictionary(): Map<String, Int> = dictionary.entries
        .associateBy({ it.value }) { it.key }

    fun getLanguageModel(): Map<List<Int>, Map<Int, Double>> = internalLanguageModel

    fun trainModel(documents: Sequence<String>) {
        val tokens = documents.flatMap { tokenizer.split(it).asSequence() } // perhaps just make it a var?
        tokens.toHashSet().forEachIndexed { i, token -> dictionary[i] = token }
        val reverseDict = getReverseDictionary()
        val tokensInt = tokens.mapNotNull(reverseDict::get)
        for (i in 1..n) {
            val subGramMap = tokensInt
                .windowed(i)    // TODO perf improvement by not windowing but rather indexing!
                .groupBy({ it.dropLast(1) }, { it.last() })
                .mapValues { (_, value) ->
                    val countMap = value.groupingBy { it }.eachCount()

                    val total = countMap.values.sum().toDouble()
                    countMap.mapValues { it.value / total }.toMutableMap()
                }

            internalLanguageModel.putAll(subGramMap)
        }
    }

    /** There is a little bug in this one it seems...*/
    fun trainModelMutable(documents: Sequence<String>) {
        val tokens = documents.flatMap { tokenizer.split(it).asSequence() }
        val ngram = NGram<Int>(n)
        tokens.toHashSet().forEachIndexed { i, token -> dictionary[i] = token }
        val reverseDict = getReverseDictionary()
        tokens
            .mapNotNull(reverseDict::get)
            .flatMap {
                ngram.add(it)
                if (ngram.count() == n) ngram.getAllNgrams().asSequence() // TODO make a sequence inside method!
                else emptySequence()
            }
            .forEach { tokenNGram ->
                val subGram = tokenNGram.dropLast(1)
                internalLanguageModel
                    .getOrPut(subGram) { mutableMapOf() }
                    .apply { put(tokenNGram.last(), getOrDefault(tokenNGram.last(), defaultValue) + 1.0) }
            }
        internalLanguageModel
            .values
            .forEach { mMap ->
                val allProbs = mMap.values.sum()
                mMap.keys.forEach { key -> mMap[key] = mMap.getValue(key) / allProbs }
            }
    }
}

object A {
    @JvmStatic
    fun main(args: Array<String>) {
        val lm = LanguageModel2(n = 3)
        javaClass.getResourceAsStream("/shakespeare.txt").bufferedReader().useLines {
            val text = it.joinToString("\n")
            println(
                measureTimeMillis { (1..5).forEach { lm.trainModel(sequenceOf(text)) } } / 1000.0 / 5
            )
            println(lm.getDictionary().entries.take(5))
            println(lm.getLanguageModel().entries.take(3))
        }
    }
}
/**
[0=frowning, 1=Reserve, 2=busses, 3=Twenty, 4=Knocks]
[[]={33339=8.456373734483583E-4, 217=1.7343895144885217E-4, 51=0.04367917909482505, 13548=1.1268632438767231E-4, 2379=0.0025878659365899353
 */