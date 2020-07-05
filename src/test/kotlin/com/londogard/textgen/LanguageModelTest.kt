package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel2
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import kotlin.test.Test

class LanguageModelTest {
    private val testText =
        sequenceOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")

    @Test
    fun makeModel() {
        val languageModel = LanguageModel2(n = 3)
        languageModel.trainModelMutable(testText)
        val dict = languageModel.getDictionary()
        val rd = languageModel.getReverseDictionary()
        val lm = languageModel.getLanguageModel()

        dict.values.toSet() shouldContain "Hello"
        lm[listOf(rd["Hello"])]?.values?.size shouldBeEqualTo 1
        lm[listOf(rd["Hello"])]?.get(rd["dear"]) shouldBeEqualTo 1.0
        lm[listOf(rd["dear"])]?.size shouldBeEqualTo 2
        lm[listOf(rd["dear"])]?.get(rd["you"])  shouldBeEqualTo 0.5
    }
}