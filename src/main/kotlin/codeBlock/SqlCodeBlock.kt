package codeBlock

import html.Html
import java.lang.StringBuilder
import java.util.*
import util.*

object SqlCodeBlock {

    private enum class HighlightSegment(val cssClass: String, val regex: Regex = Regex("")) {
        COMMENT_SINGLE_LINE("comment", Regex("--(.*)$", RegexOption.MULTILINE)),
        COMMENT_MULTI_LINE("comment", Regex("/\\*(.*?)\\*/", RegexOption.DOT_MATCHES_ALL)),

        STRING("data", Regex("'(.*?)'")),
        NUMBER("data", Regex("\\b[-+]?\\d+(\\.\\d+([uUlLfF])?)?\\b")), // TODO
        VALUE("data"),

        KEYWORD("keyword")
    }

    private class HighlightList(
            val starts: MutableList<HighlightSegment> = LinkedList(),
            var ends: Int = 0
    )

    fun toHtml(code: String, blockCssClassName: String? = null): String {
        val tags: Array<HighlightList> = Array(code.length){ HighlightList() }
        val notHighlighted: StringBuilder = StringBuilder(code);

        findAndRemove(notHighlighted, tags, HighlightSegment.COMMENT_SINGLE_LINE);
        findAndRemove(notHighlighted, tags, HighlightSegment.COMMENT_MULTI_LINE);
        findAndRemove(notHighlighted, tags, HighlightSegment.STRING);
        findAndRemove(notHighlighted, tags, HighlightSegment.NUMBER);
        findAndRemove(notHighlighted, tags, HighlightSegment.KEYWORD, keywords)
        findAndRemove(notHighlighted, tags, HighlightSegment.VALUE, values)

        return getHighlighted(code, tags, blockCssClassName)
    }

    private fun findAndRemove(code: StringBuilder, tags: Array<HighlightList>, highlightSegment: HighlightSegment){
        val regex: Regex = highlightSegment.regex
        val match: Sequence<MatchResult> = regex.findAll(code)
        match.forEach {
            tags[it.range.start].starts.add(highlightSegment)
            tags[it.range.endInclusive].ends ++
            code.set(it.range, ' ')
        }
    }

    private fun findAndRemove(code: StringBuilder, tags: Array<HighlightList>, highlightSegment: HighlightSegment, names: Array<String>) {
        names.forEach { keyword: String ->
            val regex: Regex = Regex("\\b${keyword}\\b", RegexOption.IGNORE_CASE)
            val match: Sequence<MatchResult> = regex.findAll(code)
            match.forEach { matchResult: MatchResult ->
                tags[matchResult.range.start].starts.add(highlightSegment)
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
            "ELSE",
            "END",
            "EXCEPT",
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
            "INTERSECT",
            "INTO",
            "IS",
            "JOIN",
            "KEY",
            "LEFT",
            "LIKE",
            "LIMIT",
            "NOT",
            // "NULL", value
            "OFFSET",
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
            "SIMILAR",
            "TABLE",
            "THEN",
            "TO",
            "TOP",
            "TRUNCATE",
            "UNION",
            "UNIQUE",
            "UNKNOWN",
            "UPDATE",
            "USING",
            "VALUES",
            "VIEW",
            "WHEN",
            "WHERE"
    )

    private val values: Array<String> = arrayOf(
            "NULL",
            "TRUE",
            "FALSE"
    )
}
