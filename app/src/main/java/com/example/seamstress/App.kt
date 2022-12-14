package com.example.seamstress

import android.app.Application
import android.content.Context
import com.example.seamstress.data.SeamstressRepository
import com.example.seamstress.domain.SeamstressDataBase

class App : Application() {
    val database by lazy { SeamstressDataBase.getDatabase(this) }
    val repository by lazy { SeamstressRepository(database.customersDao()) }

}

val Context.app
    get() = applicationContext as App