import java.io.File
import java.io.IOException

abstract class Builder (
        protected val config: Config,
        protected val file: File) {

    // Html template
    protected val baseText: String = getHtmlTemplate()

    protected data class HtmlData (
            val body: String,
            val internalCss: String? = null
    )

    protected fun wrap(htmlData: HtmlData): String {
        // append the file name to <title>
        val titleRegex: Regex = Regex("(?<=<title>).*(?=</title>)", RegexOption.DOT_MATCHES_ALL)
        val titleAdjusted: String = baseText.replace(titleRegex, "$0 - ${file.name}")

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

    private fun getHtmlTemplate(): String {
        return try {
            config.configDir.resolve("template.html").readText()
        }
        catch (exception: IOException) {
            """
            <!DOCTYPE html>
            <html>
                <head>
                </head>
                <body>
                </body>
            </html>
            """.trimIndent()
        }
    }
}
