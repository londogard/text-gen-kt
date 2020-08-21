package com.londogard.textgen.search

/** [[GreedySearch]] is always taking the best probabilty for each time-step, in our case we use BeamSearch(numBeams=1) */
class GreedySearch : BeamSearch(beams = 1)