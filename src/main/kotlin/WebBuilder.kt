fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        val config: Config = Config(arg.getBasePath())
        println("---- base directory: \"${config.baseDir}\" ----")
    }
    catch (e: ArgParserException) {
        System.err.println(e.message)
    }
}
