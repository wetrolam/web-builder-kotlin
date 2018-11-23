import java.io.File

fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")

        cleanDistributionDirectory(config)

        buildHtml(config)
        copyStaticFiles(config)
        processCpp(config)
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

// Create html files
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

// Copy files that do not need a transformation
private fun copyStaticFiles(config: Config) {
    config.srcDir.walk()
            .filter {
                it.isFile && it.extension in listOf("css", "png", "gif", "svg", "txt", "pdf")
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
