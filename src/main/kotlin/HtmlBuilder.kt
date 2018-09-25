import java.io.File

class HtmlBuilder(config: Config, file: File) : Builder(config, file) {

    private fun expandMacros(input: String): String {
        // <a>.cpp</a> <a>.cpp.html</a>
        val aRegex: Regex = Regex("#A\\((.*?)\\)")
        val aOutput: String = input.replace(aRegex){
            val sourcePath: String = it.groupValues[1]
            val sourceName: String = File(sourcePath).name
            val htmlPath: String = it.groupValues[1] + ".html"

            """<a href="${sourcePath}"> ${sourceName} </a> <a href="${htmlPath}"> (html) </a>"""
        }

        return aOutput
    }

    fun toHtml(): String {
        val text: String = file.readText()
        val body: String = expandMacros(text)
        return wrap(HtmlData(body))
    }
}
