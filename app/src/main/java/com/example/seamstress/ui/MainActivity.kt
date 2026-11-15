package com.example.seamstress.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.seamstress.ui.navigation.SeamstressNavHost
import com.example.seamstress.ui.theme.SeamstressTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Тулбар красится в primary под статус-баром — иконки баров светлые там и тёмные внизу.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { false }
        )

        setContent {
            SeamstressTheme {
                SeamstressNavHost()
            }
        }
    }
}
