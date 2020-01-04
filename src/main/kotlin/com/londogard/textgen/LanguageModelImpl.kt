package com.londogard.textgen

import com.londogard.textgen.backends.BackendLM
import com.londogard.textgen.backends.NGramWordLM
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.system.measureTimeMillis

@ImplicitReflectionSerializer
class LanguageModelImpl(
    override val pretrainedModels: PretrainedModels,
    override val generationLevel: GenerationLevel,
    override val n: Int = 10
) : LanguageModel {
    private val logger = logger().value
    private val model: BackendLM<String>

    init {
        model = NGramWordLM(n)
        when (generationLevel) {
            GenerationLevel.CHAR -> throw NotImplementedError("Word Generation is not implemented yet.")
            else -> Unit
        }
        when (pretrainedModels) {
            PretrainedModels.CUSTOM -> Unit
            else -> model.loadModel(pretrainedModels.path)
        }
    }

    override fun generateText(prefix: String, n: Int, temperature: Double): String {
        return (1..n)
            .fold(prefix.ngramNormalize().toList()) { acc, _ -> acc + model.predictNext(acc, temperature) }
            .fold("") { acc, itr -> if (itr.first().isLetterOrDigit()) "$acc $itr" else "$acc$itr" }
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

    override fun changeModelToCustom(path: String): Unit = model.loadModel(path, false)

    override fun changeModelToPretrained(pretrainedModel: PretrainedModels): Unit = model.loadModel(pretrainedModel.path)
}