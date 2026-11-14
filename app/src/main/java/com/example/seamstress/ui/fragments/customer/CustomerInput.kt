package com.example.seamstress.ui.fragments.customer

/**
 * Разбирает поле баланса. Пустая строка считается нулём,
 * запятая воспринимается как десятичный разделитель.
 * Возвращает null, если введено не число.
 */
internal fun parseBalance(raw: CharSequence?): Float? {
    val text = raw?.toString()?.trim().orEmpty()
    if (text.isEmpty()) return 0f
    return text.replace(',', '.').toFloatOrNull()
}
