import java.io.File

class ArgParserException(message: String) : Exception(message) {
}

class ArgParser(val args: Array<String>) {

    fun getBasePath(): File {
        if (args.size == 1) {
            val baseDir: File = File(args[0])
            if (!baseDir.isDirectory()) {
                throw ArgParserException("\"${baseDir.absolutePath}\" is not a directory")
            }
            return baseDir
        } else {
            throw ArgParserException("Incorrect number of arguments (set one parameter containing a base project directory)")
        }
    }
}
