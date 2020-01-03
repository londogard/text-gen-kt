package com.londogard.textgen

import com.londogard.textgen.backends.BackendLM
import com.londogard.textgen.backends.NGramWordLM
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.system.measureTimeMillis

@ImplicitReflectionSerializer
class LanguageModelImpl(
    override val pretrainedModels: PretrainedModels,
    override val generationLevel: GenerationLevel,
    override val n: Int
) : LanguageModel {
    private val model: BackendLM<String>
    private val logger = logger().value

    init {
        model = NGramWordLM(0.3, emptyMap(), 5)
        when (pretrainedModels) {
            PretrainedModels.CUSTOM -> Unit
            else -> model.loadModel(getResourcePath(pretrainedModels.path))
        }
        when (generationLevel) {
            GenerationLevel.CHAR -> throw NotImplementedError("Word Generation is not implemented yet.")
            else -> Unit
        }
    }

    private fun getResourcePath(path: String): String = this::class.java.getResource(path).path

    override fun generateText(prefix: String, n: Int, temperature: Double): String {
        return (1..n).fold(prefix) { acc, _ -> "$acc ${model.predictNext(acc)}" }
    }

    private fun Number.format(digits: Int) = "%.${digits}f".format(this)

    override fun createCustomModel(path: String, name: String, oneDocumentPerLine: Boolean) {
        logger.info("Training model")
        val timeUsed = measureTimeMillis {
            model.trainModel(path, oneDocumentPerLine)
        }
        logger.info("Training completed in ${(timeUsed / 1000.0).format(2)} seconds.")
        model.saveModel(name)
        logger.info("Model saved as $name")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val model = LanguageModelImpl(PretrainedModels.CUSTOM, GenerationLevel.WORD, 15)
            model.createCustomModel("/shakespeare.txt", "ngram_10.cbor", false)
            println(model.generateText("", 150, 0.3))
        }
    }
}