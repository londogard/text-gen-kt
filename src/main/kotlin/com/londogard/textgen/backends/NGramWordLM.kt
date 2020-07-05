package com.londogard.textgen.backends

import com.londogard.textgen.NGram
import com.londogard.textgen.ngramNormalize
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import java.io.File
import kotlin.math.min
import kotlin.random.Random

@ImplicitReflectionSerializer
class NGramWordLM(
    override val n: Int,
    override var internalLanguageModel: Map<String, Map<String, Double>> = emptyMap()) : BackendLM<String>() {
    //private val stringDouble =  MapSerializer(stringSerializer, doubleSerializer)
    //override val mapSerializer: KSerializer<Map<String, Map<String, Double>>> = MapSerializer(stringSerializer, stringDouble)

    override fun predictNext(input: String, temperature: Double): String =
        TODO("Implement this, don't forget to not remove \n etc")

    @InternalSerializationApi
    override fun loadModel(path: String, resource: Boolean) {
        TODO()
    }

    override fun saveModel(path: String) {
        TODO()
    }

    @ImplicitReflectionSerializer
    override fun trainModel(path: String, oneDocumentPerLine: Boolean) {
        val ngram = NGram<String>(n)

        File(javaClass.getResource(path).path).useLines { lines ->
            val words = lines
                .map { it.ngramNormalize() }
                .flatMap { line ->
                    if (oneDocumentPerLine) (padStartList + line + padEndList).asSequence()
                    else line + "\n"
                }.toList()

            val internalModel = words
                .map {
                    ngram.add(it)
                    if (ngram.count() == n) ngram.getAllNgrams()
                    else emptyList()
                }
                .filter { it.isNotEmpty() }
                .flatten()
                .groupBy(keySelector = { it }, valueTransform = { true })
                .mapValues { it.value.size.toDouble() }
                .filter { it.key.size == 1 || it.value > 3.0 }

            val totalCount = internalModel.filterKeys { it.size == 1 }.values.sum()

            val precomputedModel = internalModel
                .mapValues { (key, value) ->
                    (value / (internalModel[key.dropLast(1)] ?: totalCount))
                }

            internalLanguageModel = precomputedModel
                .entries
                .groupBy( { it.key.dropLast(1).joinToString(" ") } , { it.key.last() to it.value })
                .mapValues { it.value.toMap() }
            //internalLanguageModel = internalModel
            //    .mapValues { (key, value) ->
            //        (0.4.pow(n - key.size)) * value / (internalModel[key.dropLast(1)] ?: totalCount)
            //    }

            // TODO add Kneser-Ney Smooth
            //val discountByN = (1..n).map { i ->
            //    val ndValues = modelByN[i]?.filterValues { it in listOf(1.0, 2.0) } ?: emptyMap()
            //    val single = ndValues.filterValues { it == 1.0 }.size
            //    val twice = ndValues.filterValues { it == 2.0 }.size
            //    i to (single.toDouble() / (single + twice))
            //}
            //val internalKneserNeyPrecompute = internalModel
            //    .mapValues { (key, value) -> max(value - 1, 0.0) }
        }
    }

    override fun predictNext(input: List<String>, temperature: Double): String {
        val keys = (min(input.size, n) downTo 0).map {
            input.takeLast(it).joinToString(" ")
        }

        val options = keys.asSequence()
            .mapNotNull { key -> internalLanguageModel[key]?.entries }
            .flatMap { it.sortedByDescending { subEntry -> subEntry.value }.take(10).asSequence() }
            .take(10)
            .toList()

        var selection = Random.nextDouble(options.sumByDouble { it.value } * temperature)

        return options
            .shuffled()
            .dropWhile {
                selection -= it.value
                selection > 0
            }
            .first().key
    }
}