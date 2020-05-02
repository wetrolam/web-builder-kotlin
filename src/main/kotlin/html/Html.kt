package html

object Html {

    // Encode reserved HTML characters to character entities
    fun encode(letter: Char): String {
        return when(letter) {
            '<' -> "&lt;"
            '>' -> "&gt;"
            '&' -> "&amp;"
            '\\'-> "&bsol;"
            else -> letter.toString()
        }
    }
}