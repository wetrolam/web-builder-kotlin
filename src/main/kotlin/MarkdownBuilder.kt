import java.io.File
import java.io.IOException
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.ext.tables.TablesExtension

class MarkdownBuilder(config: Config, file: File): Builder(config, file) {

    fun toHtml(): String {
        val markdown: String = file.readText()
        val html: String = markdownToHtml(markdown)
        return wrap(HtmlData(html, getMarkdownStyle()))
    }

    private fun markdownToHtml(input: String): String {
        val options: MutableDataSet = MutableDataSet()
        options.set(Parser.EXTENSIONS, listOf(
                TablesExtension.create()
            )
        )

        val parser: Parser = Parser.builder(options).build()
        val renderer: HtmlRenderer = HtmlRenderer.builder(options).build()

        val document: Node = parser.parse(input)
        val html: String = renderer.render(document)

        return html
    }

    private fun getMarkdownStyle(): String {
        return try {
            config.configDir.resolve("md.css").readText()
        }
        catch (exception: IOException) {
            defaultMarkdownStyle
        }
    }

    private fun transformCusomMarks(input: String): String {

        // replace #R
        var id: Int = 0;
        val solution = input.replace(Regex("<p>#R</p>")){
                id++
                "<label for=\"solution_${id}\">zobraz riesenie</label><input type=\"checkbox\" value=\"hodnota\" id=\"solution_${id}\"/>"
        }

        // replace other custom marks
        val replacements: Map<String, String> = mapOf(
                "#DU" to "<span class=\"homework\">Domáca úloha</span>",
                "#CV" to "<span class=\"lecture\">Na cvičení</span>",
                "#university" to "<span class=\"db_university\">(databáza: <i class=\"fas fa-university\"></i> univerzita)</span>",
                "#pagila" to "<span class=\"db_pagila\">(databáza: <i class=\"fas fa-video\"></i> pagila)</span>",
                "#restaurant" to "<span class=\"db_restaurant\">(databáza: <i class=\"fas fa-utensils\"></i> reštaurácia)</span>"
        )
        var tmp: String = solution
        replacements.forEach{
            tmp = tmp.replace(it.key, it.value)
        }

        return tmp
    }
}

private val defaultMarkdownStyle: String = """
    h1 {
        color: blue;
    }
""".trimIndent()
