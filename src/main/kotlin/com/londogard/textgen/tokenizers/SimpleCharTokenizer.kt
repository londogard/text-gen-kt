package com.londogard.textgen.tokenizers

class SimpleCharTokenizer: Tokenizer {
    override fun split(text: String): List<String> =
        List(text.length) { i -> text[i].toString() }
}