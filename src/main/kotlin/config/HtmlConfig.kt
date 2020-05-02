package config

import java.io.File

data class HtmlConfig (
        var charset: String = "UTF-8",
        var title: String = "",
        var author: String = "",
        var favicon: File = File("favicon.png")
)
