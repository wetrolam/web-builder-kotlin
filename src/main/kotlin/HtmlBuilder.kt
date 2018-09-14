import java.io.File

data class HtmlData (
        val body: String,
        val internalCss: String? = null
)

// Builder of html files
class HtmlBuilder(val config: Config) {

    // Html template content
    private val baseText: String = config.configDir.resolve("template.html").readText()

    // Build a html file from 'srcFile'. Use 'bodyTransform' for processig the input file
    fun exec(srcFile: File, bodyTransform: (Config, String)->HtmlData, changeFileName: ((File)->File)? = null) {
        // read the input file
        val srcText: String = srcFile.readText()

        // process and wrap the content of the file
        val htmlData: HtmlData = bodyTransform(config, srcText)
        val wrappedText: String = wrapByTemplate(htmlData, srcFile.name)

        // write the output file
        val distFile: File = config.distFileOf(srcFile, changeFileName)
        distFile.parentFile.mkdirs()
        distFile.writeText(wrappedText)
    }

    private fun wrapByTemplate(htmlData: HtmlData, srcFileName: String): String {
        // append the file name to <title>
        val titleRegex: Regex = Regex("(?<=<title>).*(?=</title>)", RegexOption.DOT_MATCHES_ALL)
        val titleAdjusted: String = baseText.replace(titleRegex, "$0 - $srcFileName")

        // add internal <style>
        val styleAdjusted: String =
                if(htmlData.internalCss != null) {
                    val styleRegex: Regex = Regex("(?=</head>)", RegexOption.DOT_MATCHES_ALL)
                    titleAdjusted.replace(styleRegex, "\n<style>\n${htmlData.internalCss}\n</style>\n")
                }
                else {
                    titleAdjusted
                }

        // fill <body>
        val bodyRegex: Regex = Regex("(?<=<body>).*(?=</body>)", RegexOption.DOT_MATCHES_ALL)
        val bodyAdjusted: String = styleAdjusted.replace(bodyRegex, "\n\n${htmlData.body}\n\n")

        return bodyAdjusted
    }

    fun expandMacros(config: Config, input: String): HtmlData{
        // <a>.cpp</a> <a>.cpp.html</a>
        val aRegex: Regex = Regex("#A\\((.*?)\\)")
        val output: String = input.replace(aRegex){
            val sourceFile: File = File(it.groupValues[1])
            val htmlFile: File = File(it.groupValues[1] + ".html")
            "<a href=\"${sourceFile}\"> ${sourceFile.name} </a> <a href=\"${htmlFile}\"> (html) </a>"
        }

        return HtmlData(body = output)
    }
}
