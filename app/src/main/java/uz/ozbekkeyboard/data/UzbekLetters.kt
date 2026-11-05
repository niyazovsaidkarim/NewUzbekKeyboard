package uz.ozbekkeyboard.data

object UzbekLetters {

    // Tutuq sign variations
    const val TUTUQ_STANDARD = "ʼ" // U+02BC Modifier Letter Apostrophe
    const val TUTUQ_ASCII = "'"    // Standard single quote / apostrophe

    // Key variants map for lowercase
    private val VARIANTS_LOWER = mapOf(
        "o" to listOf("ö", "ó", "ò", "ô", "õ"),
        "g" to listOf("ğ"),
        "s" to listOf("ş", "ß"),
        "c" to listOf("ç"),
        "a" to listOf("á", "à", "ä", "â", "ã"),
        "e" to listOf("é", "è", "ë", "ê"),
        "u" to listOf("ü", "ú", "ù", "û"),
        "i" to listOf("í", "ì", "ï", "î"),
        "n" to listOf("ñ"),
        "'" to listOf(TUTUQ_STANDARD, "’", "`", "«", "»"),
        "\"" to listOf("«", "»", "“", "”"),
        "-" to listOf("—", "–", "_"),
        "." to listOf("...", "!", "?", ","),
        "?" to listOf("¿", "!", "?!"),
        "!" to listOf("¡", "!?")
    )

    // Key variants map for uppercase
    private val VARIANTS_UPPER = mapOf(
        "O" to listOf("Ö", "Ó", "Ò", "Ô", "Õ"),
        "G" to listOf("Ğ"),
        "S" to listOf("Ş"),
        "C" to listOf("Ç"),
        "A" to listOf("Á", "À", "Ä", "Â", "Ã"),
        "E" to listOf("É", "È", "Ë", "Ê"),
        "U" to listOf("Ü", "Ú", "Ù", "Û"),
        "I" to listOf("Í", "Ì", "Ï", "Î"),
        "N" to listOf("Ñ"),
        "'" to listOf(TUTUQ_STANDARD, "’", "`", "«", "»"),
        "\"" to listOf("«", "»", "“", "”"),
        "-" to listOf("—", "–", "_"),
        "." to listOf("...", "!", "?", ","),
        "?" to listOf("¿", "!", "?!"),
        "!" to listOf("¡", "!?")
    )

    fun getVariants(label: String, isUppercase: Boolean): List<String> {
        val map = if (isUppercase) VARIANTS_UPPER else VARIANTS_LOWER
        return map[label] ?: if (isUppercase) {
            VARIANTS_LOWER[label.lowercase()]?.map { it.uppercase() } ?: emptyList()
        } else {
            VARIANTS_LOWER[label] ?: emptyList()
        }
    }

    fun hasVariants(label: String): Boolean {
        val lower = label.lowercase()
        return VARIANTS_LOWER.containsKey(lower) || VARIANTS_LOWER.containsKey(label)
    }

    /**
     * Primary Uzbek variant for quick or direct accent mapping if needed.
     * o -> ö, g -> ğ, s -> ş, c -> ç
     */
    fun getPrimaryUzbekVariant(char: Char): Char? {
        return when (char) {
            'o' -> 'ö'
            'O' -> 'Ö'
            'g' -> 'ğ'
            'G' -> 'Ğ'
            's' -> 'ş'
            'S' -> 'Ş'
            'c' -> 'ç'
            'C' -> 'Ç'
            else -> null
        }
    }
}
