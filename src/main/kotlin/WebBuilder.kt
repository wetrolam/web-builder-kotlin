fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")

        cleanDistributionDirectory(config)

        buildHtml(config)
    }
    catch (e: ArgParserException) {
        System.err.println(e.message)
    }
}

// Create a fresh distribution directory (an output directory in a user web project)
fun cleanDistributionDirectory(config: Config) {
    config.distDir.deleteRecursively()
    config.distDir.mkdir()
}

// Create html files
fun buildHtml(config: Config) {
    val htmlBuilder: HtmlBuilder = HtmlBuilder(config)
    config.srcDir.walk()
            .filter { it.isFile && it.extension == "html"}
            .forEach { htmlBuilder.exec(it, htmlBuilder::expandMacros) }
}
