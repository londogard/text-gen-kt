package com.londogard.textgen.tokenizers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [[SimpleCharTokenizer]] is a simple tokenizer that extract every char into a list. */
@Serializable
@SerialName("tokenizer")
class SimpleCharTokenizer: Tokenizer {
    override val stringJoiner: String = ""

    override fun split(text: String): List<String> =
        List(text.length) { i -> text[i].toString() }
}