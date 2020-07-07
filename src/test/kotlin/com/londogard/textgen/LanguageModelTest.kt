package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBe
import kotlin.test.Test

class LanguageModelTest {
    private val testText =
        listOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")

    @Test
    fun makeModel() {
        val languageModel = LanguageModel(n = 3)
        languageModel.trainModel(testText)
        val dict = languageModel.getDictionary()
        val rd = languageModel.getReverseDictionary()
        val lm = languageModel.getLanguageModel()
        val unigramLm = languageModel.getUnigramProbs().toMap()

        dict.values.toSet() shouldContain "Hello"
        unigramLm[rd["Hello"]] shouldNotBe null
        lm[listOf(rd["Hello"])]?.let { res -> res shouldContain (rd["dear"] to 1.0) }
        lm[listOf(rd["dear"])]?.size shouldBeEqualTo 2
        lm[listOf(rd["dear"])]?.let { res -> res shouldContain (rd["you"] to 0.5) }
    }
}