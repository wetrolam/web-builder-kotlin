package builder

import config.Config
import java.io.File
import java.io.IOException
import java.lang.StringBuilder

abstract class Builder (
        protected val config: Config,
        protected val file: File) {

    protected data class HtmlData (
            val body: String,
            val internalCss: String? = null
    )

    protected fun wrap(htmlData: HtmlData): String {
        val html: StringBuilder = StringBuilder()

        html.append("<!DOCTYPE html>\n")
        html.append("<html>\n")
        html.append("    <head>\n")
        html.append("        <meta charset=\"${config.html.charset}\">\n")
        html.append("        <title>${config.html.title} - ${file.nameWithoutExtension}</title>\n")

        if(config.html.author !="") {
            html.append("        <meta name=\"author\" content=\"${config.html.author}\">\n")
        }

        val favicon: File = relativePath(file, config.html.favicon)
        html.append("        <link rel=\"shortcut icon\" type=\"image/png\" href=\"${favicon}\"/>\n")

        getConfigCssFiles().forEach{
            val css: File = relativePath(file, it)
            html.append("        <link rel=\"stylesheet\" type=\"text/css\" href=\"${css}\">\n")
        }

        if(htmlData.internalCss !=null) {
            html.append("        <style>\n${htmlData.internalCss}\n        </style>\n")
        }

        html.append("    </head>\n")
        html.append("    <body>\n")
        html.append("${htmlData.body}\n")
        html.append("    </body>\n")
        html.append("</html>\n")

        return html.toString()
    }

    private fun relativePath(from: File, to: File): File {
        return config.srcDir.relativeTo(from.parentFile).resolve(to.name)
    }

    private fun getConfigCssFiles() : Iterable<File> {
        return config
                .configDir
                .walk()
                .filter {
                    it.isFile && it.extension =="css"
                }
                .asIterable()
    }
}
