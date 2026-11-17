package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.dresses.Dress
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressDao
import com.example.seamstress.domain.dresses.DressMetric
import com.example.seamstress.domain.dresses.DressMetricRow
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.dresses.DressRow

class DressesRepository(private val dressDao: DressDao) {

    fun getDressesByCustomerId(customerId: Long): LiveData<List<DressRow>> =
        dressDao.getDressesByCustomerId(customerId)

    fun getDressById(id: Long): LiveData<DressRow?> = dressDao.getDressById(id)

    suspend fun getDressByIdOnce(id: Long): Dress? = dressDao.getDressByIdOnce(id)

    fun getDressMetrics(dressId: Long): LiveData<List<DressMetricRow>> =
        dressDao.getDressMetrics(dressId)

    fun getDressPhotos(dressId: Long): LiveData<List<DressPhoto>> =
        dressDao.getDressPhotos(dressId)

    suspend fun getDressPhotosOnce(dressId: Long): List<DressPhoto> =
        dressDao.getDressPhotosOnce(dressId)

    fun getDressComments(dressId: Long): LiveData<List<DressComment>> =
        dressDao.getDressComments(dressId)

    suspend fun getDressCommentsOnce(dressId: Long): List<DressComment> =
        dressDao.getDressCommentsOnce(dressId)

    suspend fun reorderDressComments(comments: List<DressComment>) =
        dressDao.reorderDressComments(comments)

    suspend fun insertDressWithData(
        dress: Dress,
        metrics: List<DressMetric>,
        photos: List<DressPhoto>,
        comments: List<DressComment>
    ): Long = dressDao.insertDressWithData(dress, metrics, photos, comments)

    suspend fun updateDressWithMetrics(dress: Dress, metrics: List<DressMetric>) =
        dressDao.updateDressWithMetrics(dress, metrics)

    suspend fun deleteDressById(id: Long) = dressDao.deleteDressById(id)

    suspend fun updateDressMetricValue(dressId: Long, metricId: Long, value: Float) =
        dressDao.updateDressMetricValue(dressId, metricId, value)

    suspend fun addDressMetric(metric: DressMetric) = dressDao.addDressMetric(metric)

    /** Мерки соседних изделий клиента — подсказка при снятии мерки с карточки. */
    suspend fun getCustomerDressMetrics(customerId: Long, excludeDressId: Long): List<DressMetric> =
        dressDao.getCustomerDressMetrics(customerId, excludeDressId)

    suspend fun insertDressPhoto(photo: DressPhoto): Long = dressDao.insertDressPhoto(photo)

    suspend fun deleteDressPhoto(photo: DressPhoto) = dressDao.deleteDressPhoto(photo)

    suspend fun insertDressComment(comment: DressComment): Long =
        dressDao.insertDressComment(comment)

    suspend fun updateDressComment(comment: DressComment) = dressDao.updateDressComment(comment)

    suspend fun deleteDressComment(comment: DressComment) = dressDao.deleteDressComment(comment)
}
