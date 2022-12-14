package com.example.seamstress.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import com.example.seamstress.domain.measured.measurements.MeasuredParameter
import com.example.seamstress.domain.measured.measurements.MeasuredParameterDao
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao

@Database(entities = [Customers::class, Metric::class, MeasuredParameter::class], version = 1, exportSchema = false)
abstract class SeamstressDataBase : RoomDatabase() {
    abstract fun customersDao(): CustomersDao
    abstract fun metricDao(): MetricDao
    abstract fun measurementsDao(): MeasuredParameterDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: SeamstressDataBase? = null

        fun getDatabase(context: Context): SeamstressDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SeamstressDataBase::class.java,
                    "seamstress_db"
                )
                    .createFromAsset("pre_seamstress")
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}