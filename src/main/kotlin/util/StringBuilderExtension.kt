package utility

internal fun StringBuilder.set(range: IntRange, char: Char) {
    range.forEach {
        this[it] = char
    }
}
