package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.DressesRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.dresses.Dress
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressMetric
import com.example.seamstress.domain.dresses.DressMetricRow
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.dresses.DressRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_PATTERN = "dd.MM.yyyy"

class DressViewModel(application: Application) : AndroidViewModel(application) {

    private val dressesRepository: DressesRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)
        dressesRepository = DressesRepository(db.dressDao())
    }

    /** Изделия клиента из Room: список обновляется сам после записи. */
    fun dressesByCustomer(customerId: Long): LiveData<List<DressRow>> =
        dressesRepository.getDressesByCustomerId(customerId)

    fun dressById(id: Long): LiveData<DressRow?> = dressesRepository.getDressById(id)

    fun dressMetrics(id: Long): LiveData<List<DressMetricRow>> =
        dressesRepository.getDressMetrics(id)

    fun dressPhotos(id: Long): LiveData<List<DressPhoto>> = dressesRepository.getDressPhotos(id)

    fun dressComments(id: Long): LiveData<List<DressComment>> =
        dressesRepository.getDressComments(id)

    private val _dressSaved = Channel<Long>(Channel.BUFFERED)
    val dressSaved: Flow<Long> = _dressSaved.receiveAsFlow()

    /**
     * Создаёт изделие вместе с мерками, фото и комментариями одной транзакцией.
     * [values] — пары «id метрики → значение» (копия из замера, набора или снятая в форме).
     * Дата создания — служебное поле, ставится здесь.
     */
    fun addDress(
        customerId: Long,
        name: String,
        classId: Long,
        price: Long?,
        values: List<Pair<Long, Float>>,
        photos: List<String> = emptyList(),
        comments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val createdAt = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
            val dress = Dress(
                id = 0L,
                name = name,
                classId = classId,
                customerId = customerId,
                price = price,
                createdAt = createdAt
            )
            val metrics = values.map { (metricId, value) ->
                DressMetric(id = 0L, dressId = 0L, metricId = metricId, value = value)
            }
            val photoRows = photos.map { DressPhoto(id = 0L, dressId = 0L, path = it) }
            val commentRows = comments.map {
                DressComment(id = 0L, dressId = 0L, text = it, createdAt = createdAt)
            }
            _dressSaved.send(
                dressesRepository.insertDressWithData(dress, metrics, photoRows, commentRows)
            )
        }
    }

    private val _dressUpdated = Channel<Long>(Channel.BUFFERED)
    val dressUpdated: Flow<Long> = _dressUpdated.receiveAsFlow()

    /** Правка шапки и полная замена значений мерок; клиент и дата создания не меняются. */
    fun updateDress(
        dressId: Long,
        name: String,
        classId: Long,
        price: Long?,
        values: List<Pair<Long, Float>>
    ) {
        viewModelScope.launch {
            val existing = dressesRepository.getDressByIdOnce(dressId) ?: return@launch
            val dress = existing.copy(name = name, classId = classId, price = price)
            val metrics = values.map { (metricId, value) ->
                DressMetric(id = 0L, dressId = dressId, metricId = metricId, value = value)
            }
            dressesRepository.updateDressWithMetrics(dress, metrics)
            _dressUpdated.send(dressId)
        }
    }

    private val _dressDeleted = Channel<Long>(Channel.BUFFERED)
    val dressDeleted: Flow<Long> = _dressDeleted.receiveAsFlow()

    /** Удаление изделия: мерки, фото и комментарии уходят каскадом. */
    fun deleteDress(dressId: Long) {
        viewModelScope.launch {
            dressesRepository.deleteDressById(dressId)
            _dressDeleted.send(dressId)
        }
    }

    fun addPhoto(dressId: Long, path: String) {
        viewModelScope.launch {
            dressesRepository.insertDressPhoto(DressPhoto(id = 0L, dressId = dressId, path = path))
        }
    }

    fun deletePhoto(photo: DressPhoto) {
        viewModelScope.launch { dressesRepository.deleteDressPhoto(photo) }
    }

    fun addComment(dressId: Long, text: String) {
        viewModelScope.launch {
            val createdAt = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
            dressesRepository.insertDressComment(
                DressComment(id = 0L, dressId = dressId, text = text, createdAt = createdAt)
            )
        }
    }

    fun deleteComment(comment: DressComment) {
        viewModelScope.launch { dressesRepository.deleteDressComment(comment) }
    }
}
