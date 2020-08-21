package com.londogard.textgen.utils

/** [[PadUtil]] includes padding for Strings and Chars. */
object PadUtil {
    const val padStart: Char = '\u0002'
    const val padEnd: Char = '\u0003'
    const val padStartStr: String = "$padStart "
    const val padEndStr: String = " $padEnd"

    fun String.padStartEnd(n: Int): String =
        "${padStartStr.repeat(n)}$this${padEndStr.repeat(n)}"
}