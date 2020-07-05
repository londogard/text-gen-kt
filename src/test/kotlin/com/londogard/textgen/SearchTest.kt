package com.londogard.textgen

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.languagemodels.LanguageModel2
import com.londogard.textgen.predict.GreedyBackoff
import com.londogard.textgen.predict.StupidBackoff
import com.londogard.textgen.search.*
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveSize
import org.junit.BeforeClass
import kotlin.test.Test

class SearchTest {
    companion object {
        private val languageModel = LanguageModel2(n = 3)
        lateinit var lm: InternalLanguageModel
        lateinit var reverseDict: Map<String, Int>
        lateinit var dict: Map<Int, String>
        @JvmStatic
        @BeforeClass
        fun setup() {
            val testText = sequenceOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")
            languageModel.trainModel(testText)
            lm = languageModel.getLanguageModel()
            reverseDict = languageModel.getReverseDictionary()
            dict = languageModel.getDictionary()
        }
    }

    @Test
    fun testGreedySearch() {
        val search = GreedySearch()
        val greedySearch = search.search(1, 50, 3, lm, GreedyBackoff())

        greedySearch shouldHaveSize 1
        val text = greedySearch[0].joinToString(" ", transform = dict::getValue)
        text shouldContain "dear , who do you think you're ? I'd like to kick some ass tonight dear you !"
    }

    @Test
    fun testSampleSearch() {
        val search = SampleSearch()
        val sampleSearch = search.search(3, 50, 3, lm, GreedyBackoff())

        val text = sampleSearch[0].joinToString(" ", transform = dict::getValue)
        sampleSearch shouldHaveSize 3
        text shouldContain "dear , who do you think you're ? I'd like to kick some ass tonight dear you !"
    }

    @Test
    fun testBeamSearch() {
        val search = BeamSearch(3)
        val beamSearch = search.search(3, 50, 3, lm, StupidBackoff())

        val text = beamSearch[0].joinToString(" ", transform = dict::getValue)

        beamSearch shouldHaveSize 3
        text shouldContain "dear , who do you think you're ? I'd like to kick some ass tonight dear you !"
    }

    @Test
    fun testTopPSearch() {
        val search = TopPSampleSearch(0.9)
        val topPSearch = search.search(3, 50, 3, lm, StupidBackoff())

        val text = topPSearch[0].joinToString(" ", transform = dict::getValue)
        topPSearch shouldHaveSize 3
        text shouldContain "I'd like to kick some ass tonight"
    }

    @Test
    fun testTopKSearch() {
        val search = TopKSampleSearch(7)
        // For topK the GreedyBackoff is more fun, but Stupid is testable
        val topKSearch = search.search(3, 50, 3, lm, StupidBackoff())

        val text = topKSearch[0].joinToString(" ", transform = dict::getValue)
        topKSearch shouldHaveSize 3
        text shouldContain "I'd like to kick some ass tonight"
    }
}