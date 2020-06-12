/**
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
# beam search
def beam_search_decoder(data, k):
	sequences = [[list(), 0.0]]
	# walk over each step in sequence
	for row in data:
		all_candidates = list()
		# expand each current candidate
		for i in range(len(sequences)):
			seq, score = sequences[i]
			for j in range(len(row)):
				candidate = [seq + [j], score - log(row[j])]
				all_candidates.append(candidate)
		# order all candidates by score
		ordered = sorted(all_candidates, key=lambda tup:tup[1])
		# select k best
		sequences = ordered[:k]
	return sequences
*/
class BeamSearch: Search {
    fun search(data: List<List<Double>>, beams: Int = 1): List<Int> {
        val sequences = listOf(listOf() to 0.0) // format
    }
}