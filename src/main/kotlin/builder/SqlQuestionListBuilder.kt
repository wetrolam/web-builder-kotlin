package builder

import codeBlock.SqlCodeBlock
import config.Config
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

/*private*/ class Question {
    lateinit var label: String
    lateinit var db: String
    lateinit var assignment: String
    lateinit var solution: String

    fun toHtml(): String {
        val checkBoxId: String = UUID.randomUUID().toString()

        return """
            <b>${label}</b> databáza: ${db}
            <div class="question-sql">
                ${assignment}
                <input id="${checkBoxId}" type="checkbox" class="question-sql">
                <label for="${checkBoxId}" class="question-sql"></label>
                ${SqlCodeBlock.toHtml(solution, "question-sql")}
            </div>
        """.trimIndent()
    }
}

/*private*/ class QuestionList {
    lateinit var topic: String
    lateinit var instruction: String
    lateinit var questionList: List<Question>

    fun toHtml(): String {
        val htmlQuestionList: String = questionList
                .map {
                    it.toHtml()
                }
                .joinToString(separator = "\n")

        return """
            <h2>${topic}</h2>
            <p>${instruction}</p>
            ${htmlQuestionList}
        """.trimIndent()
    }
}

class SqlQuestionListBuilder(config: Config, file: File) : Builder(config, file) {

    fun toHtml(): String {
        val yaml: Yaml = Yaml(Constructor(QuestionList::class.java))
        val stream: InputStream = file.inputStream()
        val questionList: QuestionList = yaml.load(stream)

        val html: String = createHeader() + '\n' + questionList.toHtml()

        return wrap(HtmlData(html, getCss()))
    }

    private fun createHeader(): String {
        return "<h1>${config.html.title}</h1>"
    }

    private fun getCss(): String {
        return try {
            config.configDir.resolve("sql.question.css").readText()
        }
        catch (exception: IOException) {
            defaultCss
        }
    }
}

private val defaultCss: String = """
    pre.question-sql {
        display: none;
    }
    input.question-sql {
        display: none;
    }
    input:checked.question-sql ~ pre {
        display: block;
    }
    div.question-sql {
        border: 1px solid lightgray;
        border-radius: 0.5rem;
        padding: 0.5rem;
        margin: 0.5rem;
    }
    label.question-sql {
        background-color: #e7e7e7;
        box-shadow: inset 0 2px 3px rgba(255,255,255,0.2), inset 0 -2px 3px rgba(0,0,0,0.2);
        border-radius: 4px;
        display: inline-block;
        padding: 2px 5px;
        cursor: pointer;
    }
    input + label.question-sql:after {
        content: "show solution"; 
    }
    input:checked + label.question-sql:after {
        content: "hide solution"; 
    }
"""
