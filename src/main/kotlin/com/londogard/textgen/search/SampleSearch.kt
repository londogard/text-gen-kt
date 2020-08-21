package com.londogard.textgen.search

/** [[SampleSearch]] is a greedy variant where you sample until you reach p=1. */
class SampleSearch : TopPSampleSearch(p = 1.0)