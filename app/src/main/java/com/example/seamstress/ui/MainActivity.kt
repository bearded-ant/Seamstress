package com.example.seamstress.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.seamstress.ui.navigation.SeamstressNavHost
import com.example.seamstress.ui.theme.SeamstressTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SeamstressTheme {
                SeamstressNavHost()
            }
        }
    }
}
