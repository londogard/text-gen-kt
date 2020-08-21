package com.londogard.textgen.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** [logger] is a simple logger tool, currently not used. */
fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}