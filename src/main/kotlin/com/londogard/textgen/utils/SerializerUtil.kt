package com.londogard.textgen.utils

import com.londogard.textgen.Config
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import java.io.File

/**
 * Serialization Util that simplifies the serialization of the model
 * Makes use of kotlinx.serialization and serializes using CBOR (Compact Binary Object Representation).
 */
object SerializerUtil {
    private val configSerializer: KSerializer<Config> = Config.serializer()

    fun serializeConfig(path: String, config: Config): Unit =
        Cbor.dump(configSerializer, config).let(File(path)::writeBytes)

    fun deserializeConfig(data: ByteArray): Config = Cbor.load(configSerializer, data)
    fun deserializeConfigByAbsolutePath(path: String): Config = deserializeConfig(File(path).readBytes())

    internal fun deserializeConfigByResource(name: String): Config = javaClass.getResourceAsStream(name)
        .readBytes()
        .let(this::deserializeConfig)
}