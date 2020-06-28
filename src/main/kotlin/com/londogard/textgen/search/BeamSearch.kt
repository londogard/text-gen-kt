import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min

/**
 * Setup:
 * 	Vocab: Index to Token (Char/Word)
 * 	SearchTechnique: Search
 * 	Params: numWords etc
 *
 */


/**	
 * Interface should be extensible through LambdaFuncs or "Plugins" rather than flags.
 * 		Flags works to begin with though...
 */

// TODO
//  	no-repeat; optimize that option
// 		add exclusion (or let user know they need to do it!)
fun ngramPenalty(tokens: List<Int>, n: Int = 2, penalty: Double=1.0): Double {
	if (tokens.size <= n) return 0.0

	val pen = min(abs(penalty), 1.0)
	val ngram = tokens.takeLast(n)

	return tokens	// TODO performance improvement by sliding over list rather than creating sublists
		.dropLast(1)
		.asSequence()
		.windowed(n) // scan could prove heplful?
		.count { it == ngram } * pen
}

class BeamSearch {
	fun searchLazily(dataGeneration: (List<Int>) -> Float,
					 beams: Int = 1,
					 vocabSize: Int,
					 // earlyStopping: Boolean = true, TODO add earlyStoppingSupport!
					 maxTokens: Int = 50,
					 numBeamsReturn: Int = beams): List<Pair<List<Int>, Float>> {
		var sequences = listOf(listOf<Int>() to 0f)
		(0..maxTokens).forEach { _ ->
			val allCadidates = mutableListOf<Pair<List<Int>, Float>>()
			for (i in sequences.indices) {
				val (seq, score) = sequences[i]
				for (j in 0..vocabSize) {
					val candidate = seq + listOf(j) to score - ln(dataGeneration(seq))
					allCadidates.add(candidate)
				}
			}
			allCadidates.sortBy { it.second }
			sequences = allCadidates.take(beams)
		}
		return sequences.take(numBeamsReturn)
	}

	// Allow lazy generation or something like taht to generate next probs!
    fun search(data: List<List<Double>>, beams: Int = 1): List<Pair<List<Int>, Float>> {
        var sequences = listOf(listOf<Int>() to 0f) // format
		data.forEach { row ->
			val allCadidates = mutableListOf<Pair<List<Int>, Float>>()
			for (i in sequences.indices) {
				val (seq, score) = sequences[i]
				for (j in row.indices) {
					val candidate = seq + listOf(j) to score - ln(row[j].toFloat())
					allCadidates.add(candidate)
				}
			}
			allCadidates.sortBy { it.second }
			sequences = allCadidates.take(beams)
        }
		return sequences
    }
}
/**
object A {
	@JvmStatic
	fun main(args: Array<String>) {
		val data = listOf(listOf(0.1, 0.2, 0.3, 0.4, 0.5),
			listOf(0.5, 0.4, 0.3, 0.2, 0.1),
			listOf(0.1, 0.2, 0.3, 0.4, 0.5),
			listOf(0.5, 0.4, 0.3, 0.2, 0.1),
			listOf(0.1, 0.2, 0.3, 0.4, 0.5),
			listOf(0.5, 0.4, 0.3, 0.2, 0.1),
			listOf(0.1, 0.2, 0.3, 0.4, 0.5),
			listOf(0.5, 0.4, 0.3, 0.2, 0.1),
			listOf(0.1, 0.2, 0.3, 0.4, 0.5),
			listOf(0.5, 0.4, 0.3, 0.2, 0.1))
		BeamSearch().search(data, 3).forEach { println(it) }
		/** Should be
		 ([4, 0, 4, 0, 4, 0, 4, 0, 4, 0], 6.931471805599453)
		 ([4, 0, 4, 0, 4, 0, 4, 0, 4, 1], 7.154615356913663)
		 ([4, 0, 4, 0, 4, 0, 4, 0, 3, 0], 7.154615356913663)
		 */
	}
}**/