package com.helios.dialer.domain

import com.helios.dialer.data.model.Contact

object T9SearchEngine {
    private val map = mapOf(
        '2' to "abc", '3' to "def", '4' to "ghi", '5' to "jkl",
        '6' to "mno", '7' to "pqrs", '8' to "tuv", '9' to "wxyz"
    )

    fun filter(contacts: List<Contact>, query: String): List<Contact> {
        val q = query.trim()
        if (q.isEmpty()) return contacts

        return contacts.mapNotNull { contact ->
            val normalizedNumber = normalizeNumber(contact.number)
            val normalizedQuery = normalizeNumber(q)
            val numberMatch = normalizedNumber.contains(normalizedQuery)
            val nameMatch = matchesName(contact.name, q)
            val score = when {
                normalizedNumber == normalizedQuery -> 0
                numberMatch -> 1
                nameMatch && contact.name.startsWith(q, ignoreCase = true) -> 2
                nameMatch -> 3
                else -> null
            }
            score?.let { it to contact }
        }.sortedBy { it.first }.map { it.second }
    }

    fun matches(name: String, query: String): Boolean = matchesName(name, query)

    private fun matchesName(name: String, query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        if (name.lowercase().contains(q)) return true
        if (!q.all { it.isDigit() }) return false

        val lettersOnly = name.lowercase().filter { it.isLetter() }
        if (lettersOnly.length >= q.length && t9PrefixMatches(lettersOnly, q)) return true

        return name.lowercase()
            .split(Regex("\\s+"))
            .any { word -> word.length >= q.length && t9PrefixMatches(word, q) }
    }

    private fun t9PrefixMatches(word: String, query: String): Boolean {
        query.forEachIndexed { index, digit ->
            val allowed = map[digit] ?: return false
            if (index >= word.length || word[index] !in allowed) return false
        }
        return true
    }

    fun normalizeNumber(value: String): String = value.filter { it.isDigit() || it == '+' }
}
