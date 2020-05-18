package com.londogard.textgen

import smile.nlp.normalize
import smile.nlp.words
import java.util.*

class NGram<T>(private val n: Int) {
    private val list = LinkedList<T>()
    private val dictionary =  mutableSetOf<T>()

    fun addAll(collection: Collection<T>): LinkedList<T> = list.apply {
        dictionary.addAll(collection)
        for (i in 0..size-n) pop()
    }

    fun add(element: T): LinkedList<T> = list.apply {
        if (size == n) pop()
        dictionary.add(element)
        add(element)
    }

    fun getNgrams(): List<T> = list.toList()
    fun getAllNgrams(): List<List<T>> = List(n) { i -> list.take(i+1) }

    fun count(): Int = list.size

    fun getDictionary(): Set<T> = dictionary.toSet()
}
fun String.ngramNormalize(): Sequence<String> = this
    .replace("<br/>", "\n")
    .replace("</br>", "\n")
    .replace("&quot;", "'")
    .replace("</?\\w+/?>".toRegex(), "")
    .toLowerCase()
    .normalize()
    .words("none")
    .asSequence()