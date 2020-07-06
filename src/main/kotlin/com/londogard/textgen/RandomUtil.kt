package com.londogard.textgen

import kotlin.random.Random

object RandomUtil {
    val random: Random = Random
    fun nextDouble(seed: Int) = Random.nextDouble()
}