package com.londogard.textgen

import java.util.*

class NGram<T>(private val n: Int) {
    private val list = LinkedList<T>()

    fun addAll(collection: Collection<T>): LinkedList<T> = list.apply {
        addAll(collection)
        for (i in 0..size-n) pop()
    }

    fun add(element: T): LinkedList<T> = list.apply {
        if (size == n) pop()
        add(element)
    }
    fun getNgrams(): List<T> = list.toList()

}
