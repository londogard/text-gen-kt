package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import java.io.File
import java.nio.file.Paths

object SerializerUtil {
    // TODO add int -> short & double -> float possibilities to save space!
    private val configSerializer: KSerializer<Config> = Config.serializer()
    private val cborSerializer = Cbor()

    fun serializeLanguageModel(pathToFolder: String, languageModel: LanguageModel) {
        serializeConfig(Paths.get(pathToFolder, "config.cbor").toAbsolutePath().toString(), languageModel.getConfig())
    }

    fun readLanguageModel(pathToFolder: String): LanguageModel {
        val config = deserializeConfigByAbsolutePath(Paths.get(pathToFolder, "config.cbor").toAbsolutePath().toString())
        val lm = LanguageModel(n = config.n)
        lm.initByConfig(config)
        return lm
    }

    fun serializeConfig(path: String, config: Config): Unit = cborSerializer
        .dump(configSerializer, config)
        .let(File(path)::writeBytes)

    fun deserializeConfig(data: ByteArray): Config = cborSerializer.load(configSerializer, data)
    fun deserializeConfigByAbsolutePath(path: String): Config = deserializeConfig(File(path).readBytes())
    internal fun deserializeConfigByResource(name: String): Config = javaClass.getResourceAsStream(name)
        .readBytes()
        .let(this::deserializeConfig)
}