package uz.ozbekkeyboard.layout

import uz.ozbekkeyboard.R
import uz.ozbekkeyboard.ime.KeyboardMode

object KeyboardLayout {

    fun createNumberRow(): List<Key> {
        val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        return numbers.map { num ->
            Key(
                primaryText = num,
                shiftedText = num,
                type = KeyType.NUMBER,
                weight = 1.0f
            )
        }
    }

    fun createAlphabetRows(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createNumberRow())
        }

        // Row 1: Q W E R T Y U I O P (10 keys, weight 1.0 = 10.0)
        val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map { letter ->
            Key(
                primaryText = letter,
                shiftedText = letter.uppercase(),
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }
        rows.add(row1)

        // Row 2: A S D F G H J K L (9 keys, weight 1.0 with natural Samsung centering)
        val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { letter ->
            Key(
                primaryText = letter,
                shiftedText = letter.uppercase(),
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }
        rows.add(row2)

        // Row 3: [Shift] Z X C V B N M [Backspace] (1.5 + 7.0 + 1.5 = 10.0)
        val row3 = mutableListOf<Key>()
        row3.add(
            Key(
                primaryText = "Shift",
                type = KeyType.SHIFT,
                weight = 1.5f,
                iconResId = R.drawable.ic_shift
            )
        )
        val middleLetters = listOf("z", "x", "c", "v", "b", "n", "m").map { letter ->
            Key(
                primaryText = letter,
                shiftedText = letter.uppercase(),
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }
        row3.addAll(middleLetters)
        row3.add(
            Key(
                primaryText = "Del",
                type = KeyType.BACKSPACE,
                weight = 1.5f,
                iconResId = R.drawable.ic_backspace
            )
        )
        rows.add(row3)

        // Row 4: [!#1] [Globe] [,] [SPACE] [.] [ENTER]
        rows.add(createBottomRow(isSymbolMode = false))

        return rows
    }

    fun createSymbolPage1Rows(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createNumberRow())
        }

        // Row 1: + × ÷ = / _ < > [ ] (10 keys, weight 1.0 = 10.0)
        val row1 = listOf("+", "×", "÷", "=", "/", "_", "<", ">", "[", "]").map {
            Key(primaryText = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        rows.add(row1)

        // Row 2: ! @ # £ % ^ & * ( ) (10 keys, weight 1.0 = 10.0)
        val row2 = listOf("!", "@", "#", "£", "%", "^", "&", "*", "(", ")").map {
            Key(primaryText = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        rows.add(row2)

        // Row 3: [1/2] - ' " : ; , ? [Backspace] (1.5 + 7.0 + 1.5 = 10.0)
        val row3 = mutableListOf<Key>()
        row3.add(
            Key(
                primaryText = "1/2",
                type = KeyType.SWITCH_PAGE,
                weight = 1.5f
            )
        )
        val row3Symbols = listOf("-", "'", "\"", ":", ";", ",", "?").map {
            Key(
                primaryText = it,
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }
        row3.addAll(row3Symbols)
        row3.add(
            Key(
                primaryText = "Del",
                type = KeyType.BACKSPACE,
                weight = 1.5f,
                iconResId = R.drawable.ic_backspace
            )
        )
        rows.add(row3)

        // Row 4: [ABC] [Globe] [,] [SPACE] [.] [ENTER]
        rows.add(createBottomRow(isSymbolMode = true))

        return rows
    }

    fun createSymbolPage2Rows(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createNumberRow())
        }

        // Row 1: ` ~ \ | { } € $ ₽ ₺ (10 keys, weight 1.0 = 10.0)
        val row1 = listOf("`", "~", "\\", "|", "{", "}", "€", "$", "₽", "₺").map {
            Key(primaryText = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        rows.add(row1)

        // Row 2: ° • ○ ● □ ■ ♡ ◇ (8 keys, weight 1.25 = 10.0)
        val row2 = listOf("°", "•", "○", "●", "□", "■", "♡", "◇").map {
            Key(primaryText = it, type = KeyType.CHARACTER, weight = 1.25f)
        }
        rows.add(row2)

        // Row 3: [2/2] ☆ · « » ¡ ¿ § [Backspace] (1.5 + 7.0 + 1.5 = 10.0)
        val row3 = mutableListOf<Key>()
        row3.add(
            Key(
                primaryText = "2/2",
                type = KeyType.SWITCH_PAGE,
                weight = 1.5f
            )
        )
        val row3Symbols = listOf("☆", "·", "«", "»", "¡", "¿", "§").map {
            Key(
                primaryText = it,
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }
        row3.addAll(row3Symbols)
        row3.add(
            Key(
                primaryText = "Del",
                type = KeyType.BACKSPACE,
                weight = 1.5f,
                iconResId = R.drawable.ic_backspace
            )
        )
        rows.add(row3)

        // Row 4: [ABC] [Globe] [,] [SPACE] [.] [ENTER]
        rows.add(createBottomRow(isSymbolMode = true))

        return rows
    }

    private fun createBottomRow(isSymbolMode: Boolean): List<Key> {
        val switchText = if (isSymbolMode) "ABC" else "!#1"
        return listOf(
            Key(
                primaryText = switchText,
                type = KeyType.SWITCH_SYMBOLS,
                weight = 1.4f
            ),
            Key(
                primaryText = "Globe",
                type = KeyType.GLOBE,
                weight = 1.0f,
                iconResId = R.drawable.ic_globe
            ),
            Key(
                primaryText = ",",
                type = KeyType.COMMA,
                weight = 1.0f
            ),
            Key(
                primaryText = "Özbekça",
                type = KeyType.SPACE,
                weight = 4.2f
            ),
            Key(
                primaryText = ".",
                type = KeyType.PERIOD,
                weight = 1.0f
            ),
            Key(
                primaryText = "Enter",
                type = KeyType.ENTER,
                weight = 1.4f,
                iconResId = R.drawable.ic_enter_return
            )
        )
    }

    fun getLayoutForMode(mode: KeyboardMode, showNumberRow: Boolean): List<List<Key>> {
        return when (mode) {
            KeyboardMode.ALPHABET -> createAlphabetRows(showNumberRow)
            KeyboardMode.SYMBOLS_1 -> createSymbolPage1Rows(showNumberRow)
            KeyboardMode.SYMBOLS_2 -> createSymbolPage2Rows(showNumberRow)
        }
    }

    /**
     * Calculates the bounding coordinates for every key in the layout based on available
     * dimensions and display density.
     */
    fun calculateBounds(
        rows: List<List<Key>>,
        width: Float,
        height: Float,
        density: Float
    ) {
        if (rows.isEmpty() || width <= 0f || height <= 0f) return

        val horizontalMargin = 3f * density
        val verticalMargin = 4f * density
        val keyboardTopPadding = 4f * density
        val keyboardBottomPadding = 6f * density

        val availableHeight = height - keyboardTopPadding - keyboardBottomPadding
        val rowHeight = availableHeight / rows.size

        var currentY = keyboardTopPadding

        for (row in rows) {
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val totalMargins = horizontalMargin * (row.size * 2)
            val availableWidth = width - totalMargins
            val unitWidth = availableWidth / totalWeight

            var currentX = 0f

            for (key in row) {
                val keyWidth = key.weight * unitWidth
                val left = currentX + horizontalMargin
                val top = currentY + verticalMargin
                val right = left + keyWidth
                val bottom = currentY + rowHeight - verticalMargin

                key.bounds.set(left, top, right, bottom)
                currentX += keyWidth + (horizontalMargin * 2)
            }

            currentY += rowHeight
        }
    }
}
