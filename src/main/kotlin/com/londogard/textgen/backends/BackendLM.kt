package com.londogard.textgen.backends

import kotlinx.io.InputStream
import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import java.io.File

@ImplicitReflectionSerializer
abstract class BackendLM<T> {
    protected abstract val mapSerializer: KSerializer<Map<String, Map<T, Double>>>
    protected val stringSerializer = String::class.serializer()
    protected val doubleSerializer = Double::class.serializer()
    private val cborSerializer = Cbor()
    private val padStart: Char = '\u0002'
    private val padEnd: Char = '\u0003'
    protected abstract var internalLanguageModel: Map<String, Map<T, Double>> // Map<String, Map<T, Double>>
    protected abstract val n: Int
    val padEndList = List(n) { padEnd.toString() }
    val padStartList = List(n) { padStart.toString() }

    abstract fun predictNext(input: String, temperature: Double = 0.3): String
    abstract fun predictNext(input: List<String>, temperature: Double = 0.3): String

    abstract fun trainModel(path: String, oneDocumentPerLine: Boolean)
    abstract fun loadModel(path: String, resource: Boolean = true)
    abstract fun saveModel(path: String)

    @InternalSerializationApi
    private fun getResource(path: String): InputStream = this::class.java.getResourceAsStream(path)

    protected fun serializeMapToFile(name: String, map: Map<String, Map<T, Double>>): Unit = cborSerializer
        .dump(mapSerializer, map)
        .let { File(name).writeBytes(it) }

    protected fun readSerializedMapFromFile(name: String): Map<String, Map<T, Double>> = File(name)
        .readBytes()
        .let { cborSerializer.load(mapSerializer, it) }

    @InternalSerializationApi
    protected fun readSerializedMapFromResource(name: String): Map<String, Map<T, Double>> = getResource(name)
        .readBytes()
        .let { cborSerializer.load(mapSerializer, it) }
}