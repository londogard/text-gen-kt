package com.londogard.textgen.backends

import com.londogard.textgen.PadUtil.padEnd
import com.londogard.textgen.PadUtil.padStart
import kotlinx.io.InputStream
import kotlinx.serialization.*

/**
 * Setup:
 * 	Vocab: Index to Token (Char/Word)
 * 	SearchTechnique: Search
 * 	Params: numWords etc
 *
 */

@ImplicitReflectionSerializer
abstract class BackendLM<T> {
    protected val dictionary: Map<Int, T> = emptyMap()
    protected val internalLanguageModel2: Map<List<Int>, Map<Int, Double>> = emptyMap() // TODO potential improvement by short

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

    //protected fun serializeMapToFile(name: String, map: Map<String, Map<T, Double>>): Unit = cborSerializer
    //    .dump(mapSerializer, map)
    //    .let { File(name).writeBytes(it) }
//
    //protected fun readSerializedMapFromFile(name: String): Map<String, Map<T, Double>> = File(name)
    //    .readBytes()
    //    .let { cborSerializer.load(mapSerializer, it) }
//
    //@InternalSerializationApi
    //protected fun readSerializedMapFromResource(name: String): Map<String, Map<T, Double>> = getResource(name)
    //    .readBytes()
    //    .let { cborSerializer.load(mapSerializer, it) }
}