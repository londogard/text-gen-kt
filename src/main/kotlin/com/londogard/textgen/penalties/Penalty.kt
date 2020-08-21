package com.londogard.textgen.penalties

/**
 * [[Penalty]] is a way to penalize different behaviours given history and possible entries.
 *  Example: [[NgramPenalty]] or perhaps you want to filter out swearwords? Or a Trademark?
 */
interface Penalty {
    /**
     * Penalize entries given history returning the updated probabilities.
     */
    fun penalize(entries: List<Pair<Int, Double>>, history: List<Int>): List<Pair<Int, Double>>
}
