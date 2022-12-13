package com.example.seamstress

import android.app.Application
import android.content.Context
import com.example.seamstress.data.MeasuredParameterMockImpl
import com.example.seamstress.data.SeamstressRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.measuredParameter.MeasuredParameterRepo

class App : Application() {
    val database by lazy { SeamstressDataBase.getDatabase(this) }
    val repository by lazy { SeamstressRepository(database.clientsDao()) }

}

val Context.app
    get() = applicationContext as App