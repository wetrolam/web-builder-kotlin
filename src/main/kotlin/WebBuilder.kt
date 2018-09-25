import java.io.File

fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")

        cleanDistributionDirectory(config)

        buildHtml(config)
        copyStaticFiles(config)
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
                it.isFile && it.extension in listOf("css", "png", "gif", "txt", "pdf")
            }
            .forEach {
                val distFile: File = config.distFileOf(it)
                distFile.parentFile.mkdirs() // create a directory if not exists
                it.copyTo(distFile)
            }
}
