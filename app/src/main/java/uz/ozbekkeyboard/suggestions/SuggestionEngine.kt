package uz.ozbekkeyboard.suggestions

import uz.ozbekkeyboard.data.UzbekDictionary

class SuggestionEngine {

    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isWord = false
        var frequency = 0
    }

    private val root = TrieNode()

    init {
        // Load offline Uzbek dictionary words with assigned frequencies
        var rank = UzbekDictionary.commonWords.size
        for (word in UzbekDictionary.commonWords) {
            insert(word.lowercase(), rank--)
        }
    }

    private fun insert(word: String, freq: Int) {
        if (word.isBlank()) return
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isWord = true
        current.frequency = freq
    }

    fun getSuggestions(prefix: String, maxCount: Int = 3): List<String> {
        if (prefix.isBlank() || prefix.length < 2) return emptyList()

        val lowerPrefix = prefix.lowercase()
        var current = root
        for (char in lowerPrefix) {
            current = current.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectWords(current, StringBuilder(lowerPrefix), results)

        val isAllUpper = prefix.all { it.isUpperCase() }
        val isCapitalized = prefix.first().isUpperCase()

        return results
            .sortedByDescending { it.second }
            .take(maxCount)
            .map { (word, _) ->
                when {
                    isAllUpper -> word.uppercase()
                    isCapitalized -> word.replaceFirstChar { it.uppercase() }
                    else -> word
                }
            }
    }

    private fun collectWords(node: TrieNode, currentWord: StringBuilder, results: MutableList<Pair<String, Int>>) {
        if (results.size >= 20) return
        if (node.isWord) {
            results.add(Pair(currentWord.toString(), node.frequency))
        }
        for ((char, child) in node.children) {
            currentWord.append(char)
            collectWords(child, currentWord, results)
            currentWord.deleteCharAt(currentWord.length - 1)
        }
    }
}
