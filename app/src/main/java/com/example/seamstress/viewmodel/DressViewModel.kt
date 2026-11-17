package com.example.seamstress.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.DressesRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassItem
import com.example.seamstress.domain.dresses.Dress
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressMetric
import com.example.seamstress.domain.dresses.DressMetricRow
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.dresses.DressRow
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_PATTERN = "dd.MM.yyyy"
private const val PHOTO_DIR = "dress_photos"

@OptIn(ExperimentalCoroutinesApi::class)
class DressViewModel(application: Application) : AndroidViewModel(application) {

    private val dressesRepository: DressesRepository
    private val dressClassDao: DressClassDao
    private val metricDao: MetricDao
    private val measurementDao: MeasurementDao
    private val takenMeasurementDao: TakenMeasurementDao

    val allClasses: LiveData<List<DressClassItem>>
    val allMetrics: LiveData<List<Metric>>

    init {
        val db = SeamstressDataBase.getDatabase(application)
        dressesRepository = DressesRepository(db.dressDao())
        dressClassDao = db.dressClassDao()
        metricDao = db.metricDao()
        measurementDao = db.measurementsDao()
        takenMeasurementDao = db.takenMeasurementsDao()
        allClasses = dressClassDao.getAllClasses()
        allMetrics = metricDao.getAllMetrics()
    }

    private val selectedClassId = MutableStateFlow<Long?>(null)

    /** Метрики класса в порядке снятия — каркас формы изделия; Room Flow. */
    val classMetrics: StateFlow<List<Metric>> = selectedClassId
        .flatMapLatest { classId ->
            if (classId == null) flowOf(emptyList()) else dressClassDao.getClassMetrics(classId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadClassMetrics(classId: Long) {
        selectedClassId.value = classId
    }

    /** Замеры клиента — источник копирования значений в форму изделия. */
    fun measurementsByCustomer(customerId: Long): LiveData<List<MeasurementRow>> =
        measurementDao.getMeasurementsByCustomerId(customerId)

    /** Значения замера для копирования: пары «id метрики → значение». */
    suspend fun measurementValues(measurementId: Long): List<Pair<Long, Float>> =
        takenMeasurementDao.getTakenMeasurementOnce(measurementId).map { it.metricId to it.value }

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
            val commentRows = comments.mapIndexed { index, text ->
                DressComment(id = 0L, dressId = 0L, text = text, createdAt = createdAt, position = index)
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

    /** Удаление изделия: мерки, фото и комментарии уходят каскадом, файлы фото удаляются. */
    fun deleteDress(dressId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val photos = dressesRepository.getDressPhotosOnce(dressId)
            dressesRepository.deleteDressById(dressId)
            photos.forEach { runCatching { File(it.path).delete() } }
            _dressDeleted.send(dressId)
        }
    }

    /** Горячая правка одной мерки изделия прямо с карточки. */
    fun updateMetricValue(dressId: Long, metricId: Long, value: Float) {
        viewModelScope.launch {
            dressesRepository.updateDressMetricValue(dressId, metricId, value)
        }
    }

    fun addPhoto(dressId: Long, path: String) {
        viewModelScope.launch {
            dressesRepository.insertDressPhoto(DressPhoto(id = 0L, dressId = dressId, path = path))
        }
    }

    /** Файл под снимок камерой; создаётся до запуска камеры. */
    fun createCameraPhotoFile(dressId: Long): File {
        val dir = File(getApplication<Application>().filesDir, PHOTO_DIR).apply { mkdirs() }
        return File(dir, "${dressId}_${System.currentTimeMillis()}.jpg")
    }

    /** Снимок камерой удался — файл уже на месте, пишем строку; иначе убираем пустой файл. */
    fun addPhotoFromFile(dressId: Long, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.exists() && file.length() > 0) {
                dressesRepository.insertDressPhoto(
                    DressPhoto(id = 0L, dressId = dressId, path = file.absolutePath)
                )
            } else {
                file.delete()
            }
        }
    }

    /** Фото из галереи: копия потока в хранилище приложения, не drawable. */
    fun addPhotoFromUri(dressId: Long, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = createCameraPhotoFile(dressId)
            val copied = runCatching {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } != null
            }.getOrDefault(false)
            if (copied && file.length() > 0) {
                dressesRepository.insertDressPhoto(
                    DressPhoto(id = 0L, dressId = dressId, path = file.absolutePath)
                )
            } else {
                file.delete()
            }
        }
    }

    fun deletePhoto(photo: DressPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            dressesRepository.deleteDressPhoto(photo)
            runCatching { File(photo.path).delete() }
        }
    }

    fun addComment(dressId: Long, text: String) {
        viewModelScope.launch {
            val createdAt = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
            val position = (dressesRepository.getDressCommentsOnce(dressId).maxOfOrNull { it.position } ?: -1) + 1
            dressesRepository.insertDressComment(
                DressComment(
                    id = 0L,
                    dressId = dressId,
                    text = text,
                    createdAt = createdAt,
                    position = position
                )
            )
        }
    }

    /** Закрепление/открепление: комментарий переезжает в конец закреплённых или начало обычных. */
    fun toggleCommentPin(comment: DressComment) {
        viewModelScope.launch {
            val all = dressesRepository.getDressCommentsOnce(comment.dressId).toMutableList()
            val index = all.indexOfFirst { it.id == comment.id }
            if (index < 0) return@launch
            val updated = all.removeAt(index).copy(pinned = !comment.pinned)
            val insertIndex = if (updated.pinned) {
                all.indexOfLast { it.pinned } + 1
            } else {
                all.indexOfFirst { !it.pinned }.let { if (it < 0) all.size else it }
            }
            all.add(insertIndex, updated)
            dressesRepository.reorderDressComments(all)
        }
    }

    /** Порядок после перетаскивания: позиции перенумеровываются одной транзакцией. */
    fun reorderComments(comments: List<DressComment>) {
        viewModelScope.launch { dressesRepository.reorderDressComments(comments) }
    }

    fun updateComment(comment: DressComment, text: String) {
        viewModelScope.launch { dressesRepository.updateDressComment(comment.copy(text = text)) }
    }

    fun deleteComment(comment: DressComment) {
        viewModelScope.launch { dressesRepository.deleteDressComment(comment) }
    }
}
