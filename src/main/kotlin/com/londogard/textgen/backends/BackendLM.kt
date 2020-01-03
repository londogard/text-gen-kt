package com.londogard.textgen.backends

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.io.File

@ImplicitReflectionSerializer
abstract class BackendLM<T> {
    private val mapSerializer = (String::class.serializer().list to Double::class.serializer()).map
    private val cborSerializer = Cbor.plain
    private val padStart: Char = '\u0002'
    private val padEnd: Char = '\u0003'
    abstract var temperature: Double
    protected abstract var internalLanguageModel: Map<List<String>, Double> // We want a Trie though...!
    protected abstract val n: Int
    val padEndList = List(n) { padEnd.toString() }
    val padStartList = List(n) { padStart.toString() }

    abstract fun predictNext(input: String): String
    abstract fun predictNext(input: List<String>): String

    abstract fun trainModel(path: String, oneDocumentPerLine: Boolean)
    abstract fun loadModel(path: String)
    abstract fun saveModel(path: String)

    protected fun serializeMapToFile(name: String, map: Map<List<String>, Double>): Unit = cborSerializer
        .dump(mapSerializer, map)
        .let { File(name).writeBytes(it) }

    protected fun readSerializedMapFromFile(name: String): Map<List<String>, Double> = File(name)
        .readBytes()
        .let { cborSerializer.load(mapSerializer, it) }
}