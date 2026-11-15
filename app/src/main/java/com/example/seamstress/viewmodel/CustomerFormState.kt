package com.example.seamstress.viewmodel

/** Верхняя граница баланса в рублях. */
internal const val MAX_BALANCE = 1_000_000_000L

/** Сколько цифр должно быть в номере телефона. */
private const val PHONE_DIGITS = 11

/** Сколько цифр допустимо ввести в поле баланса. */
private const val BALANCE_DIGITS = 10

/**
 * Состояние формы клиента: то, что введено, ошибки полей и блокировка кнопки «Сохранить».
 * Живёт в [SeamstressViewModel], экран только рисует его и зовёт методы ViewModel.
 */
data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val balance: String = "",
    val nameError: Boolean = false,
    val phoneError: Boolean = false,
    val balanceError: Boolean = false,
    val saving: Boolean = false
)

/** Оставляет в поле телефона только цифры, не длиннее [PHONE_DIGITS]. */
internal fun filterPhone(raw: String): String =
    raw.filter { it.isDigit() }.take(PHONE_DIGITS)

/** Оставляет в поле баланса только цифры, не длиннее [BALANCE_DIGITS]. */
internal fun filterBalance(raw: String): String =
    raw.filter { it.isDigit() }.take(BALANCE_DIGITS)

/**
 * Разбирает поле баланса: только целое число рублей от 0 до [MAX_BALANCE].
 * Пустая строка считается нулём, всё остальное — null.
 */
internal fun parseBalance(raw: String): Long? {
    val text = raw.trim()
    if (text.isEmpty()) return 0L
    val value = text.toLongOrNull() ?: return null
    return if (value in 0..MAX_BALANCE) value else null
}

/**
 * Нормализует телефон: оставляет только цифры.
 * Пустое поле допустимо и возвращает пустую строку,
 * неверное количество цифр — null.
 */
internal fun parsePhone(raw: String): String? {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return if (digits.length == PHONE_DIGITS) digits else null
}
