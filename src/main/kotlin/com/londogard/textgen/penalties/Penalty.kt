package com.londogard.textgen.penalties

interface Penalty {
    /**
     * Penalize entries given history returning the updated probabilities.
     */
    fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>>
}
