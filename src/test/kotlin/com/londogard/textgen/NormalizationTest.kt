package com.londogard.textgen

import com.londogard.textgen.normalization.LondogardNormalization
import com.londogard.textgen.normalization.SimpleNormalization
import com.londogard.textgen.normalization.SoftmaxNormalization
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInRange
import kotlin.test.Test

class NormalizationTest {
    @Test
    fun testLondogard() {
        val normalizer = LondogardNormalization(0.8)
        val normalized = normalizer.normalize(listOf(1 to 0.5, 2 to 0.4, 3 to 0.1))

        normalized[0].second shouldBeInRange 0.75..0.76
        normalized[1].second shouldBeInRange  0.24..0.25
        normalized[2].second shouldBeInRange  2.4e-4..2.5e-4
    }

    @Test
    fun testLondogardZeroTemp() {
        val normalizer = LondogardNormalization(0.0)
        val normalized = normalizer.normalize(listOf(1 to 0.5, 2 to 0.4, 3 to 0.1))

        normalized[0].second shouldBeEqualTo 0.5
        normalized[1].second shouldBeEqualTo 0.4
        normalized[2].second shouldBeEqualTo 0.1
    }
    @Test
    fun testLondogardZeroTempAbove100percent() {
        val normalizer = LondogardNormalization(0.0)
        val normalized = normalizer.normalize(listOf(1 to 5.0, 2 to 4.0, 3 to 1.0))

        normalized[0].second shouldBeEqualTo 0.5
        normalized[1].second shouldBeEqualTo 0.4
        normalized[2].second shouldBeEqualTo 0.1
    }

    @Test
    fun testSimpleNormalization() {
        val normalizer = SimpleNormalization(0.0)
        val normalized = normalizer.normalize(listOf(1 to 0.2, 2 to 0.3, 3 to 0.5))

        normalized[0].second shouldBeEqualTo 0.2
        normalized[1].second shouldBeEqualTo 0.3
        normalized[2].second shouldBeEqualTo 0.5
    }

    @Test
    fun testSimpleNormalizationAbove100percent() {
        val normalizer = SimpleNormalization(0.0)
        val normalized = normalizer.normalize(listOf(1 to 5.0, 2 to 4.0, 3 to 1.0))

        normalized[0].second shouldBeEqualTo 0.5
        normalized[1].second shouldBeEqualTo 0.4
        normalized[2].second shouldBeEqualTo 0.1
    }

    @Test
    fun testSoftmaxNormalization() {
        val normalizer = SoftmaxNormalization(1.0)
        val normalized = normalizer.normalize(doubleArrayOf(3.0, 1.0, 0.2))
        normalized shouldBeEqualTo doubleArrayOf(0.8360188027814407, 0.11314284146556011, 0.05083835575299916)
    }
}