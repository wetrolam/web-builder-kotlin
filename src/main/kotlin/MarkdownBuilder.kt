import java.io.*

class MarkdownBuilder(config: Config, file: File): Builder(config, file) {

    fun toHtml(): String {
        val input: String = file.readText()
        val pandoc: String = executePandoc(input)
        val customMarks: String = transformCusomMarks(pandoc)
        return customMarks // return wrap(HtmlData(customMarks))
    }

    private fun executePandoc(input: String): String {
        val process: Process = ProcessBuilder(
                    "pandoc",
                    "-f", "markdown",
                    "-t", "html",
                    "-s",
                    "--metadata-file", config.configDir.resolve("config.yaml").absolutePath,
                    "--css", "md.css"
                )
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        process.outputStream.write(input.toByteArray())
        process.outputStream.bufferedWriter().close()

        // TODO stdout and stderr can be too long
        val output: String = process.inputStream.bufferedReader().readText()
        val error: String = process.errorStream.bufferedReader().readText()

        val status: Int = process.waitFor()
        if(status != 0) {
            System.err.println(error)
        }

        return output
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
                "#DU" to "Domáca úloha",
                "#CV" to "Na cvičení",
                "#university" to "(databaza: university)",
                "#sakila" to "(databaza: sakila)"
        )
        var tmp: String = solution
        replacements.forEach{
            tmp = tmp.replace(it.key, it.value)
        }

        return tmp
    }
}