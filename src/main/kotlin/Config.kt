import java.io.File

class Config(val baseDir: File) {

    val srcDir: File = this.baseDir.resolve("src")
    val distDir: File = baseDir.resolve("dist")
    val configDir: File = baseDir.resolve("config")

    /**
     * Return distribution version of 'srcFile'.
     * The returned file has changed path (...../src/..... -> ...../dist/.....) and the name (if 'changeFileName' is defined).
     * @param srcFile a source file from a user project
     * @param changeFileName change of the file name of the distribution version
     * @return distribution version of 'srcFile'
     */
    fun distFileOf(srcFile: File, changeFileName: ((File)->File)? = null): File {
        var relativeFilePath: File = srcFile.relativeTo(srcDir)

        if(changeFileName != null) {
            relativeFilePath = changeFileName(relativeFilePath)
        }

        val distFile: File = distDir.resolve(relativeFilePath)

        return distFile
    }
}
