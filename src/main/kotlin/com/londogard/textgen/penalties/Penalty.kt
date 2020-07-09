package com.londogard.textgen.penalties

interface Penalty {
    fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>>
    companion object {
        fun List<Penalty>.penalizeAll(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>> =
            fold(entries) { items, penalty -> penalty.penalize(items, history) }
    }
}
