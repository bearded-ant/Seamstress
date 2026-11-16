package com.example.seamstress

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.dresses.Dress
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressMetric
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.standards.StandardMetric
import com.example.seamstress.domain.standards.StandardSet
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Каскадное удаление: в базе не остаётся висячих строк (PRAGMA foreign_key_check пуст). */
@RunWith(AndroidJUnit4::class)
class CascadeDeleteTest {

    private lateinit var db: SeamstressDataBase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SeamstressDataBase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun count(table: String): Int =
        db.query("SELECT COUNT(*) FROM $table", emptyArray()).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun foreignKeyViolations(): Int =
        db.query("PRAGMA foreign_key_check", emptyArray()).use { it.count }

    private suspend fun insertCustomer(): Long =
        db.customersDao().insertCustomer(Customers(0L, "Клиент", null, null, 0L))

    private suspend fun insertMetric(): Long =
        db.metricDao().insertMetric(Metric(0L, 1L, "ОГ", "обхват груди", null))

    @Test
    fun deleteCustomer_removesMeasurementsTakenAndDresses() = runTest {
        val customerId = insertCustomer()
        val metricId = insertMetric()
        db.measurementsDao().insertMeasurementWithTaken(
            Measurement(0L, "замер", "01.01.2026", 1L, customerId),
            listOf(TakenMeasurement(0L, metricId, 0L, customerId, 92f))
        )
        db.dressDao().insertDressWithData(
            Dress(0L, "изделие", 1L, customerId, null, "01.01.2026"),
            listOf(DressMetric(0L, 0L, metricId, 92f)),
            listOf(DressPhoto(0L, 0L, "/data/photo.jpg")),
            listOf(DressComment(0L, 0L, "комментарий", "01.01.2026"))
        )

        val customer = db.customersDao().getCustomerById(customerId)!!
        db.customersDao().deleteCustomerWithData(customer)

        assertEquals(0, count("customers"))
        assertEquals(0, count("measurements"))
        assertEquals(0, count("taken_measurements"))
        assertEquals(0, count("dress"))
        assertEquals(0, count("dress_metric"))
        assertEquals(0, count("dress_photo"))
        assertEquals(0, count("dress_comment"))
        assertEquals(0, foreignKeyViolations())
    }

    @Test
    fun deleteDress_removesMetricsPhotosComments_keepsCustomer() = runTest {
        val customerId = insertCustomer()
        val metricId = insertMetric()
        val dressId = db.dressDao().insertDressWithData(
            Dress(0L, "изделие", 1L, customerId, 1000L, "01.01.2026"),
            listOf(DressMetric(0L, 0L, metricId, 92f)),
            listOf(DressPhoto(0L, 0L, "/data/photo.jpg")),
            listOf(DressComment(0L, 0L, "комментарий", "01.01.2026"))
        )

        db.dressDao().deleteDressById(dressId)

        assertEquals(0, count("dress"))
        assertEquals(0, count("dress_metric"))
        assertEquals(0, count("dress_photo"))
        assertEquals(0, count("dress_comment"))
        assertEquals(1, count("customers"))
        assertEquals(0, foreignKeyViolations())
    }

    @Test
    fun deleteMeasurement_removesTaken() = runTest {
        val customerId = insertCustomer()
        val metricId = insertMetric()
        val measurementId = db.measurementsDao().insertMeasurementWithTaken(
            Measurement(0L, "замер", "01.01.2026", 1L, customerId),
            listOf(TakenMeasurement(0L, metricId, 0L, customerId, 92f))
        )

        db.measurementsDao().deleteMeasurements(
            Measurement(measurementId, "замер", "01.01.2026", 1L, customerId)
        )

        assertEquals(0, count("measurements"))
        assertEquals(0, count("taken_measurements"))
        assertEquals(1, count("customers"))
        assertEquals(0, foreignKeyViolations())
    }

    @Test
    fun deleteStandardSet_removesMetrics_keepsReference() = runTest {
        val metricId = insertMetric()
        val setId = db.standardSetDao().insertSetWithMetrics(
            StandardSet(0L, "Мужчина 54"),
            listOf(StandardMetric(0L, 0L, metricId, 108f))
        )

        db.standardSetDao().deleteSetById(setId)

        assertEquals(0, count("standard_set"))
        assertEquals(0, count("standard_metric"))
        assertEquals(1, count("metrics"))
        assertEquals(0, foreignKeyViolations())
    }
}
