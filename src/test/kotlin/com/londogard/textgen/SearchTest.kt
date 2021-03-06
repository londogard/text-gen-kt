package com.londogard.textgen

import com.londogard.textgen.languagemodels.LanguageModel
import com.londogard.textgen.smoothing.GreedyBackoff
import com.londogard.textgen.smoothing.StupidBackoff
import com.londogard.textgen.search.*
import org.amshove.kluent.*
import org.junit.BeforeClass
import kotlin.test.Test

class SearchTest {
    companion object {
        lateinit var languageModel: LanguageModel
        lateinit var reverseDict: Map<String, Int>
        lateinit var dict: Map<Int, String>
        @JvmStatic
        @BeforeClass
        fun setup() {
            val testText = listOf("Hello dear, who do you think you're? I'd like to kick some ass tonight dear you!")
            //Sampling.setSeed(42)
            languageModel = LanguageModel.fit(testText, n = 3)
            reverseDict = languageModel.getReverseDictionary()
            dict = languageModel.dictionary
        }
    }

    @Test
    fun testGreedySearch() {
        val search = GreedySearch()
        val greedySearch = search.search(1, 50, 3, languageModel, GreedyBackoff())

        greedySearch shouldHaveSize 1
        val text = greedySearch[0].joinToString(" ", transform = dict::getValue)
        text shouldContain "dear , who do you think you're ? I'd like to kick some ass tonight dear you !"
    }

    @Test
    fun testSampleSearch() {
        val search = SampleSearch()
        val sampleSearch = search.search(3, 50, 3, languageModel, GreedyBackoff())

        val text = sampleSearch[0].joinToString(" ", transform = dict::getValue)
        sampleSearch shouldHaveSize 3
        text shouldContain "I'd like to kick some ass tonight dear you !"
    }

    @Test
    fun testBeamSearch() {
        val search = BeamSearch(3)
        val beamSearch = search.search(3, 50, 3, languageModel, StupidBackoff())

        val text = beamSearch[0].joinToString(" ", transform = dict::getValue)

        beamSearch shouldHaveSize 3
        text shouldContain "you think you're ? I'd like to kick some ass tonight dear"
    }

    @Test
    fun testTopPSearch() {
        val search = TopPSampleSearch(0.9)
        val topPSearch = search.search(3, 50, 3, languageModel, StupidBackoff())

        val text = topPSearch[0].joinToString(" ", transform = dict::getValue)
        topPSearch shouldHaveSize 3
        text shouldContain "I'd like to kick some ass tonight"
    }

    @Test
    fun testTopKSearch() {
        val search = TopKSampleSearch(7)
        // For topK the GreedyBackoff is more fun, but Stupid is testable
        val topKSearch = search.search(3, 50, 3, languageModel, StupidBackoff())

        val text = topKSearch[0].joinToString(" ", transform = dict::getValue)
        topKSearch shouldHaveSize 3
        text shouldContain "I'd like to kick some ass tonight"
    }

    @Test
    fun testTopKSeeedSearch() {
        val search = TopKSampleSearch(7)
        val seed = listOf("who", "do").mapNotNull(reverseDict::get)
        val topKSearch = search.search(3, 50, 3, languageModel, StupidBackoff(), seed)

        val text = topKSearch[0].joinToString(" ", transform = dict::getValue)
        topKSearch shouldHaveSize 3
        text shouldStartWith "who do"
        topKSearch.forEach { tokens -> tokens.take(2) shouldBeEqualTo seed }
    }
}