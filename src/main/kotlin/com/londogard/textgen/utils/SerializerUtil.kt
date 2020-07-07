package com.londogard.textgen.utils

import com.londogard.textgen.Config
import com.londogard.textgen.languagemodels.LanguageModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import java.io.File
import java.nio.file.Paths

object SerializerUtil {
    private val configSerializer: KSerializer<Config> = Config.serializer()

    fun serializeLanguageModel(pathToFolder: String, languageModel: LanguageModel, modelName: String? = null) {
        val name = modelName ?: "lm-${languageModel.n}-gram"

        serializeConfig(Paths.get(pathToFolder, "$name.cbor").toString(), languageModel.getConfig())
    }

    fun readLanguageModel(absolutePath: String): LanguageModel {
        val config = deserializeConfigByAbsolutePath(absolutePath)
        val lm = LanguageModel(n = config.n)
        lm.initByConfig(config)
        return lm
    }

    fun serializeConfig(path: String, config: Config): Unit =
        Cbor.dump(configSerializer, config).let(File(path)::writeBytes)

    fun deserializeConfig(data: ByteArray): Config = Cbor.load(configSerializer, data)
    fun deserializeConfigByAbsolutePath(path: String): Config = deserializeConfig(File(path).readBytes())

    internal fun deserializeConfigByResource(name: String): Config = javaClass.getResourceAsStream(name)
        .readBytes()
        .let(this::deserializeConfig)
}