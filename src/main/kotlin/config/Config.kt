package config

import java.io.File
import java.io.InputStream
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

class Config(val baseDir: File) {

    val srcDir: File = this.baseDir.resolve("src")
    val distDir: File = baseDir.resolve("dist")
    val configDir: File = baseDir.resolve("config")
    val html: HtmlConfig = loadConfig<HtmlConfig>("html.yaml")

    private inline fun <reified T>loadConfig(fileName: String): T {
        val yaml: Yaml = Yaml(Constructor(T::class.java))
        val stream: InputStream = configDir.resolve(fileName).inputStream()
        return yaml.load(stream)
    }

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
