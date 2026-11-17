package com.example.seamstress.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 7,
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

        /** Комментарии: порядок перетаскивания и закрепление; старый порядок сохраняем по id. */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dress_comment ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE dress_comment ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE dress_comment SET position = id")
            }
        }

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
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}