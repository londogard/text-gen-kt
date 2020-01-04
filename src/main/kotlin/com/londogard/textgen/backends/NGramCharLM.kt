package com.londogard.textgen.backends

import com.londogard.smile.extensions.StopWordFilter
import com.londogard.smile.extensions.normalize
import com.londogard.smile.extensions.words
import com.londogard.textgen.NGram
import kotlinx.serialization.ImplicitReflectionSerializer
import java.io.File
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess
/**
 * /// (Char::class.serializer().list to Double::class.serializer()).map
@ImplicitReflectionSerializer
class NGramCharLM(override var temperature: Double, override var internalLanguageModel: Map<List<Char>, Double>, override val n: Int) :
    BackendLM<Char>() {

    override fun predictNext(input: String): String {
        val normalizedInput = input.ngramNormalize().toList().takeLast(n - 1)
        val options = (n downTo 1)
            .asSequence()
            .map { i -> internalLanguageModel.filterKeys { it.size == i && (it.size == 1 || it.take(i-1) == normalizedInput.takeLast(i-1)) }.mapValues { it.value * 0.4.pow(n.toDouble() - i)
            }.entries }
            .map { it.sortedByDescending { l -> l.value } }
            .flatten()
            .filter { input.indexOfLast { it == '\n' } < input.length - 25 || !it.key.last().contains('\n')  }
            .take(10)
            .toList()

        var selection = Random.nextDouble(options.sumByDouble { it.value } * temperature)

        return options
            .shuffled()
            .dropWhile {
                selection -= it.value
                selection > 0
            }
            .first().key.last()
    }

    override fun loadModel(path: String) {
        internalLanguageModel = readSerializedMapFromFile(path)
    }

    override fun saveModel(path: String) {
        serializeMapToFile(path, internalLanguageModel)
    }

    fun String.ngramNormalize(): Sequence<String> = this
        .replace("<br/>", "\n")
        .replace("</br>", "\n")
        .replace("&quot;", "'")
        .replace("</?\\w+/?>".toRegex(), "")
        .toLowerCase()
        .normalize()
        .words(StopWordFilter.NONE)
        .asSequence()

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


            val totalCount = internalModel.filterKeys { it.size == 1 }.values.sum()
            val r = "\\w+".toRegex()
            val keysToRemove = internalModel
                .filter { (it.value > 1 || it.key.size == 1) && !(it.key.size == 1 && !it.key.first().matches(r)) }
                .keys
            internalLanguageModel = internalModel
                .mapNotNull { (key, value) ->
                    if (keysToRemove.contains(key)) key to (value / (internalModel[key.dropLast(1)] ?: totalCount))
                    else null
                }.toMap()

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

    override fun predictNext(input: List<String>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}**/