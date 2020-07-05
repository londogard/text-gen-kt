package com.londogard.textgen.penalties

interface Penalty {
    fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>>
}
