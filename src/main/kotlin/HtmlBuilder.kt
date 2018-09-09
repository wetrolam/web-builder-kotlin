import java.io.File

// Builder of html files
class HtmlBuilder(val config: Config) {

    // Html template content
    val baseText: String = config.srcDir.resolve("html.template").readText()

    // Build a html file from 'srcFile'. Use 'op' for processig the input file
    fun exec(srcFile: File, op: (String)->String) {
        // read the input file
        val srcText: String = srcFile.readText()

        // process and wrap the content of the file
        val processedText: String = op(srcText)
        val wrappedText: String = wrapByTemplate(processedText, srcFile.name)

        // write the output file
        val distFile: File = config.distFileOf(srcFile)
        distFile.parentFile.mkdirs()
        distFile.writeText(wrappedText)
    }

    private fun wrapByTemplate(srcText: String, srcFileName: String): String {
        // append the file name to <title>
        val titleRegex: Regex = Regex("(?<=<title>).*(?=</title>)", RegexOption.DOT_MATCHES_ALL)
        val titleText: String = baseText.replace(titleRegex, "$0 - $srcFileName")

        // fill <body>
        val bodyRegex: Regex = Regex("(?<=<body>).*(?=</body>)", RegexOption.DOT_MATCHES_ALL)
        val distText: String = titleText.replace(bodyRegex, "\n\n$srcText\n\n")

        return distText
    }

    fun expandMacros(input: String): String{
        // <a>.cpp</a> <a>.cpp.html</a>
        val aRegex: Regex = Regex("#A\\((.*?)\\)")
        val output: String = input.replace(aRegex){
            val sourceFile: File = File(it.groupValues[1])
            val htmlFile: File = File(it.groupValues[1] + ".html")
            "<a href=\"${sourceFile}\"> ${sourceFile.name} </a> <a href=\"${htmlFile}\"> (html) </a>"
        }

        return output
    }
}
