package com.example.seamstress.domain.dresses

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface DressDao {

    @Query(
        """
        select d.id, d.name, d.class_id, c.name as class_name, d.customer_id, d.price, d.created_at,
               (select p.path from dress_photo p where p.dress_id = d.id order by p.id limit 1) as photo_path
        from dress d
        left join dress_classes c on c.id = d.class_id
        where d.customer_id = :customerId
        order by d.id desc
        """
    )
    fun getDressesByCustomerId(customerId: Long): LiveData<List<DressRow>>

    @Query(
        """
        select d.id, d.name, d.class_id, c.name as class_name, d.customer_id, d.price, d.created_at,
               (select p.path from dress_photo p where p.dress_id = d.id order by p.id limit 1) as photo_path
        from dress d
        left join dress_classes c on c.id = d.class_id
        where d.id = :id
        """
    )
    fun getDressById(id: Long): LiveData<DressRow?>

    @Query("select * from dress where id = :id")
    suspend fun getDressByIdOnce(id: Long): Dress?

    @Query(
        """
        select dm.metric_id as metric_id, dm.value, m.short_name as short_name, m.name as metric_name,
               coalesce(cm.position, 1000) as position
        from dress_metric dm
        join metrics m on m.id = dm.metric_id
        join dress d on d.id = dm.dress_id
        left join dress_class_metrics cm on cm.metric_id = dm.metric_id and cm.class_id = d.class_id
        where dm.dress_id = :dressId
        order by position, dm.id
        """
    )
    fun getDressMetrics(dressId: Long): LiveData<List<DressMetricRow>>

    @Query("select * from dress_photo where dress_id = :dressId order by id")
    fun getDressPhotos(dressId: Long): LiveData<List<DressPhoto>>

    @Query("select * from dress_photo where dress_id = :dressId order by id")
    suspend fun getDressPhotosOnce(dressId: Long): List<DressPhoto>

    /** Фото всех изделий клиента — для очистки файлов при удалении клиента. */
    @Query(
        """
        select p.* from dress_photo p
        join dress d on d.id = p.dress_id
        where d.customer_id = :customerId
        """
    )
    suspend fun getPhotosByCustomerId(customerId: Long): List<DressPhoto>

    @Query("select * from dress_comment where dress_id = :dressId order by pinned desc, position, id")
    fun getDressComments(dressId: Long): LiveData<List<DressComment>>

    @Query("select * from dress_comment where dress_id = :dressId order by pinned desc, position, id")
    suspend fun getDressCommentsOnce(dressId: Long): List<DressComment>

    @Insert
    suspend fun insertDress(dress: Dress): Long

    @Insert
    suspend fun insertDressMetrics(metrics: List<DressMetric>): List<Long>

    @Insert
    suspend fun insertDressPhotos(photos: List<DressPhoto>): List<Long>

    @Insert
    suspend fun insertDressComments(comments: List<DressComment>): List<Long>

    /** Изделие и его мерки/фото/комментарии пишутся одной транзакцией: id изделия нужен дочерним. */
    @Transaction
    suspend fun insertDressWithData(
        dress: Dress,
        metrics: List<DressMetric>,
        photos: List<DressPhoto>,
        comments: List<DressComment>
    ): Long {
        val dressId = insertDress(dress)
        if (metrics.isNotEmpty()) {
            insertDressMetrics(metrics.map { it.copy(dressId = dressId) })
        }
        if (photos.isNotEmpty()) {
            insertDressPhotos(photos.map { it.copy(dressId = dressId) })
        }
        if (comments.isNotEmpty()) {
            insertDressComments(comments.map { it.copy(dressId = dressId) })
        }
        return dressId
    }

    @Update
    suspend fun updateDress(dress: Dress)

    @Query("delete from dress_metric where dress_id = :dressId")
    suspend fun deleteDressMetrics(dressId: Long)

    /** Правка изделия: шапка обновляется, значения мерок заменяются целиком новой копией. */
    @Transaction
    suspend fun updateDressWithMetrics(dress: Dress, metrics: List<DressMetric>) {
        updateDress(dress)
        deleteDressMetrics(dress.id)
        if (metrics.isNotEmpty()) {
            insertDressMetrics(metrics.map { it.copy(dressId = dress.id) })
        }
    }

    /** Изделие удаляется вместе с мерками, фото и комментариями — ON DELETE CASCADE. */
    @Query("delete from dress where id = :id")
    suspend fun deleteDressById(id: Long)

    /** Горячая правка значения мерки изделия — без замены всего набора. */
    @Query("update dress_metric set value = :value where dress_id = :dressId and metric_id = :metricId")
    suspend fun updateDressMetricValue(dressId: Long, metricId: Long, value: Float)

    /**
     * Мерки всех изделий клиента, кроме указанного — «крайние» показания соседних изделий
     * подсказываются при снятии мерки с карточки.
     */
    @Query(
        """
        select m.* from dress_metric m
        join dress d on d.id = m.dress_id
        where d.customer_id = :customerId and d.id != :excludeDressId
        """
    )
    suspend fun getCustomerDressMetrics(customerId: Long, excludeDressId: Long): List<DressMetric>

    /** Новая мерка изделия; если пара «изделие + метрика» уже есть — просто обновляется. */
    @Transaction
    suspend fun addDressMetric(metric: DressMetric) {
        if (dressMetricExists(metric.dressId, metric.metricId) > 0) {
            updateDressMetricValue(metric.dressId, metric.metricId, metric.value)
        } else {
            insertDressMetric(metric)
        }
    }

    @Query("select count(*) from dress_metric where dress_id = :dressId and metric_id = :metricId")
    suspend fun dressMetricExists(dressId: Long, metricId: Long): Int

    @Insert
    suspend fun insertDressMetric(metric: DressMetric): Long

    @Insert
    suspend fun insertDressPhoto(photo: DressPhoto): Long

    @Delete
    suspend fun deleteDressPhoto(photo: DressPhoto)

    @Insert
    suspend fun insertDressComment(comment: DressComment): Long

    @Update
    suspend fun updateDressComment(comment: DressComment)

    /** Перестановка комментариев: позиция — индекс в переданном порядке, заодно пишется закрепление. */
    @Transaction
    suspend fun reorderDressComments(comments: List<DressComment>) {
        comments.forEachIndexed { index, comment ->
            updateDressComment(comment.copy(position = index))
        }
    }

    @Delete
    suspend fun deleteDressComment(comment: DressComment)
}
