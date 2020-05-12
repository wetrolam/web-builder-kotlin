package codeBlock

import html.Html
import java.lang.StringBuilder
import java.util.*
import util.*

object SqlCodeBlock {

    private enum class HighlightSegment(val cssClass: String) {
        KEYWORD("keyword")
    }

    private class HighlightList(
            val starts: MutableList<HighlightSegment> = LinkedList(),
            var ends: Int = 0
    )

    fun toHtml(code: String, blockCssClassName: String? = null): String {
        val tags: Array<HighlightList> = Array(code.length){ HighlightList() }
        val notHighlighted: StringBuilder = StringBuilder(code);

        findAndRemoveKeywords(notHighlighted, tags)

        return getHighlighted(code, tags, blockCssClassName)
    }

    private fun findAndRemoveKeywords(code: StringBuilder, tags: Array<HighlightList>) {
        keywords.forEach { keyword: String ->
            val regex: Regex = Regex("\\b${keyword}\\b", RegexOption.IGNORE_CASE)
            val match: Sequence<MatchResult> = regex.findAll(code)
            match.forEach { matchResult: MatchResult ->
                tags[matchResult.range.start].starts.add(HighlightSegment.KEYWORD)
                tags[matchResult.range.endInclusive].ends ++
                code.set(matchResult.range, ' ')
            }
        }
    }

    private fun getHighlighted(code: String, tags: Array<HighlightList>, blockCssClassName: String?): String {
        val html: StringBuilder = StringBuilder("<pre class=\"${blockCssClassName ?: ""}\"><code>")

        for(position in 0 until code.length) {
            tags[position].starts.forEach {
                html.append("<span class=\"${it.cssClass}\">")
            }

            html.append(Html.encode(code[position]))

            repeat(tags[position].ends) {
                html.append("</span>")
            }
        }

        html.append("</code></pre>")

        return html.toString()
    }

    private val keywords: Array<String> = arrayOf(
            "ADD",
            "ALTER",
            "ALL",
            "AND",
            "ANY",
            "AS",
            "ASC",
            "BACKUP",
            "BETWEEN",
            "BY",
            "CASE",
            "CHECK",
            "COLUMN",
            "CONSTRAINT",
            "CREATE",
            "DATABASE",
            "DEFAULT",
            "DELETE",
            "DESC",
            "DISTINCT",
            "DROP",
            "EXEC",
            "EXISTS",
            "FOREIGN",
            "FROM",
            "FULL",
            "GROUP",
            "HAVING",
            "IN",
            "INDEX",
            "INNER",
            "INSERT",
            "INSERT",
            "INTO",
            "IS",
            "JOIN",
            "KEY",
            "LEFT",
            "LIKE",
            "LIMIT",
            "NOT",
            "NULL", // value
            "OR",
            "ORDER",
            "OUTER",
            "PRIMARY",
            "PROCEDURE",
            "REPLACE",
            "RIGHT",
            "ROWNUM",
            "SELECT",
            "SELECT",
            "SET",
            "TABLE",
            "TOP",
            "TRUNCATE",
            "UNION",
            "UNIQUE",
            "UNKNOWN",
            "UPDATE",
            "VALUES",
            "VIEW",
            "WHERE"
    )
}
