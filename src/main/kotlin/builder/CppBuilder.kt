package builder

import config.Config
import java.io.File
import java.io.IOException
import java.util.*
import util.*

// Variant of C/C++ source file or a line
private enum class Variant(val pattern: String) {
    ASSIGNMENT("//#Z"),
    SOLUTION("//#R");

    companion object {
        fun findByRegex(value: String): Variant {
            return enumValues<Variant>().find { it.pattern == value } ?: throw IllegalArgumentException("No enum constant $value")
        }
    }
}

// Types of highlighting segments in C/C++ source file
private enum class HighlightSegment(val cssClass: String, val regex: Regex = Regex("")) {
    COMMENT_SINGLE_LINE("comment", Regex("//(.*)$", RegexOption.MULTILINE)), // TODO
    COMMENT_MULTI_LINE("comment", Regex("/\\*(.*?)\\*/", RegexOption.DOT_MATCHES_ALL)),

    PREPROCESSOR("preprocessor", Regex("^[ \t]*(#.*)$", RegexOption.MULTILINE)),

    STRING("data", Regex("\"(.*?)\"")),
    NUMBER("data", Regex("\\b[-+]?\\d+(\\.\\d+([uUlLfF])?)?\\b")), // TODO
    CHAR("data", Regex("'(.*?)'")),

    KEYWORD("keyword")
}

// Highlighting tags of a character in a C/C++ source file
private data class HighlightList(
        val starts: MutableList<HighlightSegment> = LinkedList(),
        var ends: Int = 0
)

private class HighlightParser(cppText: String) {
    private val cpp: StringBuilder = StringBuilder(cppText)

    private val tags: Array<HighlightList> = Array(cpp.length){HighlightList()}

    private fun findAndRemove(highlightSegment: HighlightSegment){
        val regex: Regex = highlightSegment.regex
        val match: Sequence<MatchResult> = regex.findAll(cpp)
        match.forEach {
            tags[it.range.start].starts.add(highlightSegment)
            tags[it.range.endInclusive].ends ++
            cpp.set(it.range, ' ')
        }
    }

    private fun removeControlComments() {
        val regex: Regex = Regex("//#\\w+[ \t]+\\w+")
        val match: Sequence<MatchResult> = regex.findAll(cpp)
        match.forEach {
            cpp.set(it.range, ' ')
        }
    }

    private fun findAndRemoveKeywords() {
        keywords.forEach {
            val regex: Regex = Regex("\\b$it\\b")
            val match: Sequence<MatchResult> = regex.findAll(cpp)
            match.forEach {
                tags[it.range.start].starts.add(HighlightSegment.KEYWORD)
                tags[it.range.endInclusive].ends ++
                cpp.set(it.range, ' ')
            }
        }
    }

    fun exec(): Array<HighlightList> {
        removeControlComments()
        findAndRemove(HighlightSegment.COMMENT_SINGLE_LINE)
        findAndRemove(HighlightSegment.COMMENT_MULTI_LINE)
        findAndRemove(HighlightSegment.PREPROCESSOR)
        findAndRemove(HighlightSegment.NUMBER)
        findAndRemove(HighlightSegment.CHAR)
        findAndRemove(HighlightSegment.STRING)
        findAndRemoveKeywords()

        return tags
    }
}

// Represents a line in a C/C++ source file. Contains an analysis of the line
private class Line (text: String){
    val content: String // content of the line without a control comment
    val variant: Variant? // assignment/solution/both(null)
    val taskName: String?
    var switch: Boolean = false // true if it is the first line with 'taskName'
    val highlighting: Array<HighlightList> // highlighting tags
    private companion object {
        val regex: Regex = Regex("[ \t]*(//#\\w)+[ \t]+(\\w+)") // control comment in C/C++ source files
    }

    init {
        val matchResult: MatchResult? = regex.find(text)
        content = if(matchResult == null) {
                    text
                }
                else {
                    text.replace(regex, "")
                }
        variant = try {
                    Variant.findByRegex(matchResult?.groupValues?.get(1) ?: "")
                }
                catch (e: IllegalArgumentException) {
                    null
                }
        taskName = matchResult?.groupValues?.get(2)
        highlighting = HighlightParser(content).exec()
    }

    fun toHtml(): String {
        val html: StringBuilder = StringBuilder()

        for(position in 0 until content.length) {
            highlighting[position].starts.forEach {
                html.append("<span class=\"${it.cssClass}\">")
            }

            html.append(encode(content[position]))

            repeat(highlighting[position].ends) {
                html.append("</span>")
            }
        }

        return html.toString()
    }

    // Encode reserved HTML characters to character entities
    private fun encode(letter: Char): String {
        return when(letter) {
            '<' -> "&lt;"
            '>' -> "&gt;"
            '&' -> "&amp;"
            '\\'-> "&bsol;"
            else -> letter.toString()
        }
    }
}

// A builder for several C/C++ source code variants
class CppBuilder(config: Config, file: File) : Builder(config, file) {
    private val lines: List<Line>
    private val cppStyle: String = getCppStyle()

    init {
        lines = parse(file.readText())
        findSwitchPositions()
    }

    // Split C/C++ source code into lines
    private fun parse(cppText: String): List<Line>  {
        return cppText
                .lineSequence()
                .toList()
                .map <String, Line> { it: String ->
                    Line(it)
                }
    }

    // For all tasks, find first assignment or solution occurence
    private fun findSwitchPositions() {
        val allCls: MutableSet<String> = HashSet<String>()
        lines.forEach(){
            if (it.variant != null) {
                if (allCls.isEmpty() || it.taskName !in allCls) {
                    it.switch = true // will a new line with insert switch
                    allCls.add(it.taskName!!)
                }
            }
        }
    }

    fun getAssignment(): String {
        return lines.filter {
                    it.variant != Variant.SOLUTION
                }
                .joinToString(separator = "\n") {
                    it.content
                }
    }

    fun getSolution(): String {
        return lines.filter {
                    it.variant != Variant.ASSIGNMENT
                }
                .joinToString(separator = "\n") {
                    it.content
                }
    }

    fun getHighlightedAssignment(): String {
        val body: String =
                lines.filter {
                    it.variant != Variant.SOLUTION
                }
                .joinToString(prefix =  "<pre><code>", separator = "\n", postfix = "</code></pre>") {
                    it.toHtml()
                }

        return wrap(HtmlData(body, cppStyle))
    }

    fun getHighlightedSolution(): String {

        val html: StringBuilder = StringBuilder("<pre><code>")

        lines.forEach { line: Line ->
            if(line.switch) {
                html.append("""    <input type="checkbox" style="display: none" id="${line.taskName}" class="${line.taskName}"><label for="${line.taskName}">// </label>""" + "\n")
            }
            if(line.variant == Variant.ASSIGNMENT) {
                html.append("""<span class="assignment assignment_${line.taskName}">""")
            }
            if(line.variant == Variant.SOLUTION) {
                html.append("""<span class="solution solution_${line.taskName}">""")
            }

            html.append(line.toHtml())
            html.append("\n")

            if(line.variant != null) { // in listOf(Variant.ASSIGNMENT, Variant.SOLUTION)
                html.append("</span>")
            }
        }

        html.append("</code></pre>")

        val htmlData: HtmlData = HtmlData(body = html.toString(),internalCss = generateSolutionCss())
        return wrap(htmlData)
    }

    private fun getCppStyle(): String {
        return try {
            config.configDir.resolve("cpp.css").readText()
        }
        catch (exception: IOException) {
            defaultCppStyle
        }
    }

    private fun generateSolutionCss(): String {
        val dynamicCss: String =
                lines.filter {
                    it.switch
                }
                .joinToString(separator = "\n") {
                    val cls: String = it.taskName!!
                    """
                    input.${cls} ~ .solution_${cls} {
                        display: none;
                    }
                    input.${cls}:checked ~ .solution_${cls} {
                        display: initial;
                    }
                    input.${cls} ~ .assignment_${cls} {
                        display: initial;
                    }
                    input.${cls}:checked ~ .assignment_${cls} {
                        display: none;
                    }
                """.trimIndent()
                }

        return cppStyle + "\n" + dynamicCss
    }

    // Return true if the source file contains at least one specific line, else return false
    fun isForkNeeded(): Boolean {
        return lines.find { it.variant != null } != null
    }
}

private val keywords: Array<String> = arrayOf(
    "alignas",
    "alignof",
    "and",
    "and_eq",
    "asm",
    "atomic_cancel",
    "atomic_commit",
    "atomic_noexcept",
    "auto",
    "bitand",
    "bitor",
    "bool",
    "break",
    "case",
    "catch",
    "char",
    "char16_t",
    "char32_t",
    "class",
    "compl",
    "concept",
    "const",
    "constexpr",
    "const_cast",
    "continue",
    "decltype",
    "default",
    "delete",
    "do",
    "double",
    "dynamic_cast",
    "else",
    "enum",
    "explicit",
    "export",
    "extern",
    "false",
    "float",
    "for",
    "friend",
    "goto",
    "if",
    "inline",
    "int",
    "import",
    "long",
    "module",
    "mutable",
    "namespace",
    "new",
    "noexcept",
    "not",
    "not_eq",
    "nullptr",
    "operator",
    "or",
    "or_eq",
    "private",
    "protected",
    "public",
    "register",
    "reinterpret_cast",
    "requires",
    "return",
    "short",
    "signed",
    "sizeof",
    "static",
    "static_assert",
    "static_cast",
    "struct",
    "switch",
    "synchronized",
    "template",
    "this",
    "thread_local",
    "throw",
    "true",
    "try",
    "typedef",
    "typeid",
    "typename",
    "union",
    "unsigned",
    "using",
    "virtual",
    "void",
    "volatile",
    "wchar_t",
    "while",
    "xor",
    "xor_eq",

    "override",
    "final",
    "transaction_safe",
    "transaction_safe_dynamic"
)

private val preprocessor: Array<String> = arrayOf(
    "if",
    "elif",
    "else",
    "endif",
    "defined",
    "ifdef",
    "ifndef",
    "define",
    "undef",
    "include",
    "line",
    "error",
    "pragma"
)

private val defaultCppStyle: String = """
    .keyword {
        color: blue;
    }
    .comment {
        color: green;
        font-style: italic;
    }
    .preprocessor {
        color: blue;
        font-weight: bold;
    }
    .data {
        color: red;
    }

    label {
        background-color: yellow;
        box-shadow: inset 0 2px 3px rgba(255,255,255,0.2), inset 0 -2px 3px rgba(0,0,0,0.2);
        border-radius: 4px;
        display: inline-block;
        padding: 2px 5px;
        cursor: pointer;
    }

    input + label:after {
        content: "show solution";
    }

    input:checked + label:after {
        content: "hide solution";
    }

    .assignment {
        background: linear-gradient(to right,rgb(240,240,240) 1%, white 50%);
    }

    .solution {
        background: linear-gradient(to right,yellow 1%, white 50%);
    }
""".trimIndent()
