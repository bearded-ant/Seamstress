package com.example.seamstress.ui.components

import java.text.NumberFormat
import java.util.Locale

/** 92.0f → «92», 92.5f → «92.5». */
fun formatMeasure(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()

/** 89987654321 → «+7 (998) 765-43-21»; номер другой длины показывается как есть. */
fun formatPhone(phone: String?): String {
    val digits = phone?.filter(Char::isDigit).orEmpty()
    if (digits.length != 11) return phone.orEmpty()
    val n = digits.substring(1)
    return "+7 (${n.substring(0, 3)}) ${n.substring(3, 6)}-${n.substring(6, 8)}-${n.substring(8, 10)}"
}

/** 1234567 → «1 234 567 ₽» — группировка тысяч и знак рубля. */
fun formatMoney(value: Long): String =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("ru")).format(value) + " ₽"
