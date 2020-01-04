package com.londogard.textgen


enum class PretrainedModels(val path: String) {
    SHAKESPEARE("/models/shakespeare.cbor"),
    CARDS_AGAINST_WHITE("/models/cardsagainst_white.cbor"),
    CARDS_AGAINST_BLACK("/models/cardsagainst_black.cbor"),
    CUSTOM("/") // CUSTOM mode let's you train by file.
}

enum class GenerationLevel {
    WORD,
    CHAR
}

interface LanguageModel {
    val pretrainedModels: PretrainedModels
    val generationLevel: GenerationLevel
    val n: Int

    fun generateText(prefix: String, n: Int, temperature: Double = 0.5): String

    /**
     * If name exists on unit return pre-trained model
     * @param path the path to the file
     * @param name the name of the model (use: <Name>_<N>_<Level>)
     * @param oneDocumentPerLine if true we'll treat each line as a document (i.e. pad around each line). If false it'll load the full file.
     */
    fun createCustomModel(path: String, name: String, oneDocumentPerLine: Boolean = false)
}

