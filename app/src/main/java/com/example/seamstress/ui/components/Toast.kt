package com.example.seamstress.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Тост по строковому ресурсу — короткие сообщения об ошибке. */
@Composable
fun rememberToast(): (Int) -> Unit {
    val context = LocalContext.current
    return remember(context) { { resId: Int ->
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    } }
}
