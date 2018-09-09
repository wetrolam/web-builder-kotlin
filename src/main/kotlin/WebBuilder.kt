fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")

        cleanDistributionDirectory(config)
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
