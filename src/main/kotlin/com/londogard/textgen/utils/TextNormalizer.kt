package com.londogard.textgen.utils

import java.text.Normalizer

/** Weird unicode characters to ASCII equivalents
 * This list comes from
 * - http://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/current/docs/designDoc/UDF/unicode/DefaultTables/symbolTable.html
 * - https://github.com/haifengl/smile/blob/master/nlp/src/main/java/smile/nlp/normalizer/SimpleNormalizer.java
 * */
object TextNormalizer {
    fun normalize(text: String): String {
        var updatedStr = text
        if (!Normalizer.isNormalized(updatedStr, Normalizer.Form.NFKC)) {
            updatedStr = Normalizer.normalize(updatedStr, Normalizer.Form.NFKC)
        }

        updatedStr = unicodeCharMap.fold(updatedStr) { acc, (from, to) -> acc.replace(from, to) }
        updatedStr = unicodeStringMap.fold(updatedStr) { acc, (from, to) -> acc.replace(from, to) }
        updatedStr = WhitespaceRegexp.replace(updatedStr, " ")
        // updatedStr = ControlFormatChars.replace(updatedStr, "")

        return updatedStr
    }

    //private val ControlFormatChars = "[\\p{Cc}\\p{Cf}]".toRegex()
    private val WhitespaceRegexp = "[^\\S\\r\\n]+".toRegex()

    private val unicodeStringMap: List<Pair<String, String>> = listOf(
        "\u2015" to "--",
        "\u2016" to "||",
        "\u2034" to "''",
        "\u2037" to "''",
        "\u20E5" to "\\",
        "\u2216" to "\\",
        "\u2264" to "<=",
        "\u2265" to ">=",
        "\u2266" to "<=",
        "\u2267" to ">=",
    )
    private val unicodeCharMap: List<Pair<Char, Char>> = listOf(
        '\u00AB' to '"',
        '\u00AD' to '-',
        '\u02BB' to '\'',
        '\u0060' to '\'',
        '\u02BD' to '\'',
        '\u275C' to '\'',
        '\u275C' to '\'',
        '\u275B' to '\'',
        '\u00B4' to '\'',
        '\u275E' to '"',
        '\u00BB' to '"',
        '\u00F7' to '/',
        '\u01C0' to '|',
        '\u01C3' to '!',
        '\u02B9' to '\'',
        '\u02BA' to '"',
        '\u02BC' to '\'',
        '\u02C4' to '^',
        '\u02C6' to '^',
        '\u02C8' to '\'',
        '\u02CB' to '`',
        '\u02CD' to '_',
        '\u02DC' to '~',
        '\u0300' to '`',
        '\u0301' to '\'',
        '\u0302' to '^',
        '\u0303' to '~',
        '\u030B' to '"',
        '\u030E' to '"',
        '\u0331' to '_',
        '\u0332' to '_',
        '\u0338' to '/',
        '\u0589' to ':',
        '\u05C0' to '|',
        '\u05C3' to ':',
        '\u066A' to '%',
        '\u066D' to '*',
        '\u200B' to ' ',
        '\u2010' to '-',
        '\u2011' to '-',
        '\u2012' to '-',
        '\u2013' to '-',
        '\u2014' to '-',
        '\u2017' to '_',
        '\u2018' to '\'',
        '\u2019' to '\'',
        '\u201A' to ',',
        '\u201B' to '\'',
        '\u201C' to '"',
        '\u201D' to '"',
        '\u201E' to '"',
        '\u201F' to '"',
        '\u2032' to '\'',
        '\u2033' to '"',
        '\u2035' to '`',
        '\u2036' to '"',
        '\u275D' to '"',
        '\u2038' to '~',
        '\u2039' to '<',
        '\u203A' to '>',
        '\u203D' to '?',
        '\u2044' to '/',
        '\u204E' to '*',
        '\u2052' to '%',
        '\u2053' to '~',
        '\u2060' to ' ',
        '\u2212' to '-',
        '\u2215' to '/',
        '\u2217' to '*',
        '\u2223' to '|',
        '\u2236' to ':',
        '\u223C' to '~',
        '\u2303' to '~',
        '\u2329' to '<',
        '\u232A' to '>',
        '\u266F' to '#',
        '\u2731' to '*',
        '\u2758' to '|',
        '\u2762' to '!',
        '\u27E6' to '[',
        '\u27E8' to '<',
        '\u27E9' to '>',
        '\u2983' to '{',
        '\u2984' to '}',
        '\u3003' to '"',
        '\u3008' to '<',
        '\u3009' to '>',
        '\u301B' to ']',
        '\u301C' to '~',
        '\u301D' to '"',
        '\u301E' to '"',
        '\u301F' to '"',
        '\uFF02' to '"',
        '\uFEFF' to ' '
    )
}