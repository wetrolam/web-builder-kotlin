fun main(args: Array<String>) {
    try {
        val arg: ArgParser = ArgParser(args)
        println("---- base directory: \"${arg.getBasePath()}\" ----")
    }
    catch (e: ArgParserException) {
        System.err.println(e.message)
    }
}
