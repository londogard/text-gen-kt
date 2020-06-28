import com.londogard.textgen.search.Search
import kotlin.math.ln

class TopKSampling {

	/**
	 * Sampling (introduce randomness)
	 * 	Pick the next word w_t according to the probability. Using Ngram probs
	 * 	To improve this we want to keep high probs high and low lower. This is done through
	 * 		editing the 'temperature' of the softmax. I.e. distribution is sharper!
	 */

	/**
	 * TopK-Sampling
	 * 	https://huggingface.co/blog/how-to-generate#top-k-sampling
	 */

	/**
	 * n-grams, reduce % of same ngram appears twice (thrice etc). Simplest variant, %=0 if second time
	 * 	Let's try it out by setting no_repeat_ngram_size=2
	 * 	OBS:  An article generated about the city New York should not use a 2-gram penalty or otherwise,
	 * 			the name of the city would only appear once in the whole text!
	 *		  Requires a lot of fine-tuning..
	 *		  
	 */
	fun searchLazily(dataGeneration: (List<Int>) -> Float,
					 beams: Int = 1,
					 vocabSize: Int,
					 earlyStopping: Boolean = true,
					 maxTokens: Int = 50,
					 numBeamsReturn: Int = beams): List<Pair<List<Int>, Float>> {
		var sequences = listOf(listOf<Int>() to 0f)

		TODO()
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

object a {



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
}