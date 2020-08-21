package com.londogard.textgen.utils

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.tokenizers.Tokenizer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import java.io.File

/**
 * Serialization Util that simplifies the serialization of the model
 * Makes use of kotlinx.serialization and serializes using CBOR (Compact Binary Object Representation).
 */
@ExperimentalSerializationApi
object SerializerUtil {
    private val configSerializer: KSerializer<LanguageModel> = LanguageModel.serializer()
    private var format = Cbor { serializersModule = Tokenizer.module }
    fun setFormat(cbor: Cbor) {
        format = cbor
    }

    fun encodeLanguageModel(path: String, languageModel: LanguageModel): Unit =
        format.encodeToByteArray(configSerializer, languageModel).let(File(path)::writeBytes)

    fun decodeByteArrayLanguageModel(data: ByteArray): LanguageModel = format.decodeFromByteArray(configSerializer, data)
    fun decodeLanguageModelByAbsPath(path: String): LanguageModel = decodeByteArrayLanguageModel(File(path).readBytes())

    internal fun decodeLanguageModelByResource(name: String): LanguageModel = javaClass.getResourceAsStream(name)
        .readBytes()
        .let(this::decodeByteArrayLanguageModel)
}