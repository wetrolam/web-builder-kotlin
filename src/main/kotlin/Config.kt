import java.io.File

class Config(val baseDir: File) {

    val srcDir: File = this.baseDir.resolve("src")
    val distDir: File = baseDir.resolve("dist")

    fun distFileOf(srcFile: File): File {
        val relativeFilePath: File = srcFile.relativeTo(srcDir)
        val distFile: File = distDir.resolve(relativeFilePath)
        return distFile
    }
}
