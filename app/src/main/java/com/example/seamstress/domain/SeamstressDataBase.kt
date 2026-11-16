package com.example.seamstress.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import com.example.seamstress.domain.dress.DressClass
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassMetric
import com.example.seamstress.domain.dresses.Dress
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressDao
import com.example.seamstress.domain.dresses.DressMetric
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao
import com.example.seamstress.domain.standards.StandardMetric
import com.example.seamstress.domain.standards.StandardSet
import com.example.seamstress.domain.standards.StandardSetDao

@Database(
    entities = [
        Customers::class,
        Metric::class,
        Measurement::class,
        TakenMeasurement::class,
        DressClass::class,
        DressClassMetric::class,
        Dress::class,
        DressMetric::class,
        DressPhoto::class,
        DressComment::class,
        StandardSet::class,
        StandardMetric::class
    ],
    version = 6,
    exportSchema = true
)
abstract class SeamstressDataBase : RoomDatabase() {
    abstract fun customersDao(): CustomersDao
    abstract fun dressClassDao(): DressClassDao
    abstract fun metricDao(): MetricDao
    abstract fun measurementsDao(): MeasurementDao
    abstract fun takenMeasurementsDao(): TakenMeasurementDao
    abstract fun dressDao(): DressDao
    abstract fun standardSetDao(): StandardSetDao

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
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}