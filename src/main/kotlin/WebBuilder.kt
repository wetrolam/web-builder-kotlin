import builder.CppBuilder
import builder.HtmlBuilder
import builder.MarkdownBuilder
import builder.SqlQuestionListBuilder
import config.Config
import java.io.File

fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")

        cleanDistributionDirectory(config)

        buildHtml(config)
        buildMarkdown(config)
        copyConfigurationStaticFiles(config)
        copyStaticFiles(config)
        processCpp(config)
        buildSqlQuestionList(config)
    }
    catch (e: ArgParserException) {
        System.err.println(e.message)
    }
}

// Create a fresh distribution directory (an output directory in a user web project)
private fun cleanDistributionDirectory(config: Config) {
    config.distDir.deleteRecursively()
    config.distDir.mkdir()
}

// Create html files from html files
private fun buildHtml(config: Config) {
    config.srcDir.walk()
            .filter {
                it.isFile && it.extension == "html"
            }
            .forEach {
                val html: String = HtmlBuilder(config, it).toHtml()
                val distFile: File = config.distFileOf(it)
                distFile.parentFile.mkdirs() // create a directory if not exists
                distFile.writeText(html)
            }
}

// Create html files from markdown files
private fun buildMarkdown(config: Config) {
    config.srcDir.walk()
            .filter {
                it.isFile && it.extension == "md"
            }
            .forEach {
                val html: String = MarkdownBuilder(config, it).toHtml()
                val distFile: File = File(config.distFileOf(it).path.replaceAfterLast(".","html"))
                distFile.parentFile.mkdirs() // create a directory if not exists
                distFile.writeText(html)
            }
}

// Copy css and image files from the configuration directory ('webProjectDirectory/config')
private fun copyConfigurationStaticFiles(config: Config){
    config.configDir.walk()
            .filter {
                it.isFile && it.extension in listOf("css", "png", "gif", "jpg", "svg")
            }
            .forEach {
                var relativeFilePath: File = it.relativeTo(config.configDir)
                val distFile: File = config.distDir.resolve(relativeFilePath)
                it.copyTo(distFile)
            }
}

// Copy files from source directory ('webProjectDirectory/src') that do not need a transformation
private fun copyStaticFiles(config: Config) {
    config.srcDir.walk()
            .filter {
                it.isFile && it.extension in listOf("css", "png", "gif", "jpg", "svg", "txt", "pdf", "zip")
            }
            .forEach {
                val distFile: File = config.distFileOf(it)
                distFile.parentFile.mkdirs() // create a directory if not exists
                it.copyTo(distFile)
            }
}

private fun processCpp(config: Config) {
    config.srcDir.walk()
            .filter {
                it.isFile && it.extension in listOf("c", "cpp", "cc", "h", "hpp")
            }
            .forEach {
                config.distFileOf(it).parentFile.mkdirs() // create a directory if not exists

                val cppBuilder: CppBuilder = CppBuilder(config, it)

                if(cppBuilder.isForkNeeded()) {
                    val assignmentText: String = cppBuilder.getAssignment()
                    val assignmentTextFile: File = config.distFileOf(it){
                        File(it.path.substringBeforeLast(".") + "_zadanie." + it.extension)
                    }
                    assignmentTextFile.writeText(assignmentText)

                    val solutionText: String = cppBuilder.getSolution()
                    val solutionTextFile: File = config.distFileOf(it){
                        File(it.path.substringBeforeLast(".") + "_riesenie." + it.extension)
                    }
                    solutionTextFile.writeText(solutionText)

                    val assignmentHtml: String = cppBuilder.getHighlightedAssignment()
                    val assignmentHtmlFile: File = config.distFileOf(it){
                        File(it.path.substringBeforeLast(".") + "_zadanie." + it.extension + ".html")
                    }
                    assignmentHtmlFile.writeText(assignmentHtml)

                    val solutionHtml: String = cppBuilder.getHighlightedSolution()
                    val solutionHtmlFile: File = config.distFileOf(it){
                        File(it.path.substringBeforeLast(".") + "_riesenie." + it.extension + ".html")
                    }
                    solutionHtmlFile.writeText(solutionHtml)
                }
                else { // there is not a difference between an assignment and a solution
                    val assignmentText: String = cppBuilder.getAssignment()
                    val assignmentTextFile: File = config.distFileOf(it)
                    assignmentTextFile.writeText(assignmentText)

                    val assignmentHtml: String = cppBuilder.getHighlightedAssignment()
                    val assignmentHtmlFile: File = config.distFileOf(it){
                        File(it.path+".html")
                    }
                    assignmentHtmlFile.writeText(assignmentHtml)
                }
            }
}

// Create html files containing a question list from yaml files. The topic of the questions is SQL.
private fun buildSqlQuestionList(config: Config) {
    val sourceExtension: String = ".sql.questions.yaml" // composed extension of the source file type

    config.srcDir.walk()
            .filter {
                it.isFile && it.name.endsWith(sourceExtension)
            }
            .forEach {
                val html: String = SqlQuestionListBuilder(config, it).toHtml()
                val distFile: File = File(config.distFileOf(it).path.dropLast(sourceExtension.length) + ".questions.html")
                distFile.parentFile.mkdirs() // create a directory if not exists
                distFile.writeText(html)
            }
}
