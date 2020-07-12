package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.GreedyBackoff
import com.londogard.textgen.smoothing.NoSmoothing
import com.londogard.textgen.smoothing.StupidBackoff
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.BeforeClass
import kotlin.test.Test

class SmoothingTest {
    companion object {
        lateinit var languageModel: LanguageModel
        lateinit var reverseDict: Map<String, Int>
        @JvmStatic
        @BeforeClass
        fun setup() {
            val testText = listOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")
            languageModel = LanguageModel.trainModel(testText, n = 3)
            reverseDict = languageModel.getReverseDictionary()
        }
    }

    @Test
    fun testNoSmoothing() {
        val smoothing = NoSmoothing()
        val helloItem = smoothing.probabilitiesTopK(languageModel, listOf(reverseDict.getValue("Hello")))[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0
        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1,1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(languageModel, listOf(1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(languageModel, listOf(6,5)) shouldHaveSize 1
        smoothing.probabilitiesTopK(languageModel, listOf(1,5)) shouldHaveSize 0
        smoothing.probabilitiesTopK(languageModel, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }

    @Test
    fun testGreedyBackoff() {
        val smoothing = GreedyBackoff()
        val helloItem = smoothing.probabilitiesTopK(languageModel, listOf(reverseDict.getValue("Hello")), 1)[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0

        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(6,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }

    @Test
    fun testStupidBackoff() {
        val smoothing = StupidBackoff()
        val helloItem = smoothing.probabilitiesTopK(languageModel, listOf(reverseDict.getValue("Hello")), 1)[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0

        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(6,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, listOf(1,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(languageModel, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }
}