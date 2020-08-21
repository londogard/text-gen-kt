package com.londogard.textgen.tokenizers

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

interface Tokenizer {
    /** Splits the string into a list of tokens. */
    fun split(text: String): List<String>
    val stringJoiner: String

    companion object {
        val module = SerializersModule {
            polymorphic(Tokenizer::class) {
                subclass(SimpleCharTokenizer::class, SimpleCharTokenizer.serializer())
                subclass(SimpleWordTokenizer::class, SimpleWordTokenizer.serializer())
            }
        }
    }
}