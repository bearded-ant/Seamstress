package com.example.seamstress.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/** Значение LiveData и признак, что источник уже ответил. */
data class LoadedValue<T>(val value: T, val loaded: Boolean)

/**
 * `observeAsState` не отличает «ещё не пришло» от `null`: здесь первое значение источника
 * (даже `null`) помечается как [LoadedValue.loaded] — по нему показывают «не найдено».
 */
@Composable
fun <T> LiveData<T>.observeLoaded(): LoadedValue<T?> {
    var state by remember(this) { mutableStateOf(LoadedValue<T?>(null, false)) }
    DisposableEffect(this) {
        val observer = Observer<T> { value -> state = LoadedValue(value, true) }
        observeForever(observer)
        onDispose { removeObserver(observer) }
    }
    return state
}
