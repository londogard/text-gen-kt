package com.londogard.textgen.backends

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import java.io.File

@ImplicitReflectionSerializer
abstract class BackendLM<T> {
    protected abstract val mapSerializer: KSerializer<Map<List<T>, Double>>
    private val mapCharSerializer = (Char::class.serializer().list to Double::class.serializer()).map
    private val cborSerializer = Cbor.plain
    private val padStart: Char = '\u0002'
    private val padEnd: Char = '\u0003'
    protected abstract var internalLanguageModel: Map<List<T>, Double>
    protected abstract val n: Int
    val padEndList = List(n) { padEnd.toString() }
    val padStartList = List(n) { padStart.toString() }

    abstract fun predictNext(input: String, temperature: Double = 0.3): String
    abstract fun predictNext(input: List<String>, temperature: Double = 0.3): String

    abstract fun trainModel(path: String, oneDocumentPerLine: Boolean)
    abstract fun loadModel(path: String)
    abstract fun saveModel(path: String)

    protected fun serializeMapToFile(name: String, map: Map<List<T>, Double>): Unit = cborSerializer
        .dump(mapSerializer, map)
        .let { File(name).writeBytes(it) }

    protected fun readSerializedMapFromFile(name: String): Map<List<T>, Double> = File(name)
        .readBytes()
        .let { cborSerializer.load(mapSerializer, it) }
}