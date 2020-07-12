package com.londogard.textgen.tokenizers

interface Tokenizer {
    // fun split(text: String): List<String>
    /**
     * Splits the string into a list of tokens.
     */
    fun split(text: String): Array<String>

}