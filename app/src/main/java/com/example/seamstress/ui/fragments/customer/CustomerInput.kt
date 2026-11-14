package com.example.seamstress.ui.fragments.customer

/** Верхняя граница баланса в рублях. */
internal const val MAX_BALANCE = 1_000_000_000L

/** Сколько цифр должно быть в номере телефона. */
private const val PHONE_DIGITS = 11

/**
 * Разбирает поле баланса: только целое число рублей от 0 до [MAX_BALANCE].
 * Пустая строка считается нулём, всё остальное — null.
 */
internal fun parseBalance(raw: CharSequence?): Long? {
    val text = raw?.toString()?.trim().orEmpty()
    if (text.isEmpty()) return 0L
    val value = text.toLongOrNull() ?: return null
    return if (value in 0..MAX_BALANCE) value else null
}

/**
 * Нормализует телефон: оставляет только цифры.
 * Пустое поле допустимо и возвращает пустую строку,
 * неверное количество цифр — null.
 */
internal fun parsePhone(raw: CharSequence?): String? {
    val digits = raw?.toString()?.filter { it.isDigit() }.orEmpty()
    if (digits.isEmpty()) return ""
    return if (digits.length == PHONE_DIGITS) digits else null
}
