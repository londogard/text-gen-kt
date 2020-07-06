package com.londogard.textgen

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.predict.GreedyBackoff
import com.londogard.textgen.predict.NoSmoothing
import com.londogard.textgen.predict.StupidBackoff
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.BeforeClass
import kotlin.test.Test

class SmoothingTest {
    companion object {
        private val languageModel = LanguageModel(n = 3)
        lateinit var lm: InternalLanguageModel
        lateinit var reverseDict: Map<String, Int>
        @JvmStatic
        @BeforeClass
        fun setup() {
            val testText = sequenceOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")
            languageModel.trainModel(testText)
            lm = languageModel.getLanguageModel()
            reverseDict = languageModel.getReverseDictionary()
        }
    }

    @Test
    fun testNoSmoothing() {
        val smoothing = NoSmoothing()
        val helloItem = smoothing.probabilitiesTopK(lm, listOf(reverseDict.getValue("Hello")))[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0
        smoothing.probabilitiesTopK(lm, listOf(1,1,1,1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(lm, listOf(1,1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(lm, listOf(1,1)) shouldHaveSize 0
        smoothing.probabilitiesTopK(lm, listOf(6,5)) shouldHaveSize 1
        smoothing.probabilitiesTopK(lm, listOf(1,5)) shouldHaveSize 0
        smoothing.probabilitiesTopK(lm, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }

    @Test
    fun testGreedyBackoff() {
        val smoothing = GreedyBackoff()
        val helloItem = smoothing.probabilitiesTopK(lm, listOf(reverseDict.getValue("Hello")), 1)[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0

        smoothing.probabilitiesTopK(lm, listOf(1,1,1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(6,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }

    @Test
    fun testStupidBackoff() {
        val smoothing = StupidBackoff()
        val helloItem = smoothing.probabilitiesTopK(lm, listOf(reverseDict.getValue("Hello")), 1)[0]

        helloItem.first shouldBeEqualTo reverseDict.getValue("dear")
        helloItem.second shouldBeEqualTo 1.0

        smoothing.probabilitiesTopK(lm, listOf(1,1,1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,1)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(6,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, listOf(1,5)) shouldHaveSize 10
        smoothing.probabilitiesTopK(lm, emptyList(), Int.MAX_VALUE) shouldHaveSize reverseDict.size
    }
}