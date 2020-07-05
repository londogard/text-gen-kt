package com.londogard.textgen

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import java.io.File

@ImplicitReflectionSerializer
object SerializerUtil {
    //private val mapSerializer<T>: KSerializer<Map<String, Map<T, Double>>>
    // val mapSerializer: KSerializer<Map<List<Int>, Map<Int, Double>>>
    private val stringSerializer = String::class.serializer()
    private val doubleSerializer = Double::class.serializer()
    private val cborSerializer = Cbor()

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