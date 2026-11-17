package com.example.seamstress.ui.screens.dress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.seamstress.R
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressMetricRow
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.dresses.DressRow
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.ui.components.PhotoImage
import com.example.seamstress.ui.components.PhotoViewerDialog
import com.example.seamstress.ui.components.formatMeasure
import com.example.seamstress.ui.components.formatMoney

/**
 * Карточка изделия: название · фото-слайдер (загрузить/сделать фото/удалить, просмотр на весь экран) ·
 * связанные мерки (правка по тапу) · стоимость · комментарии (добавить/изменить/удалить,
 * перетаскивание и закрепление) · карандаш «Редактировать изделие» в углу фото.
 * Дата создания — служебное поле, только показывается.
 */
@Composable
fun DressCardScreen(
    dress: DressRow?,
    loaded: Boolean,
    metrics: List<DressMetricRow>,
    photos: List<DressPhoto>,
    comments: List<DressComment>,
    onEdit: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onDeletePhoto: (DressPhoto) -> Unit,
    onUpdateMetricValue: (DressMetricRow, Float) -> Unit,
    loadClassMetrics: suspend (Long) -> List<Metric>,
    loadAllMetrics: suspend () -> List<Metric>,
    loadNeighborMeasurements: suspend (Long) -> List<Triple<Long, String, String>>,
    onAddMetric: (Long, Float) -> Unit,
    onAddComment: (String) -> Unit,
    onUpdateComment: (DressComment, String) -> Unit,
    onDeleteComment: (DressComment) -> Unit,
    onToggleCommentPin: (DressComment) -> Unit,
    onReorderComments: (List<DressComment>) -> Unit
) {
    var showCommentDialog by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var editMetric by remember { mutableStateOf<DressMetricRow?>(null) }
    var showTakeMetric by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf<DressComment?>(null) }

    Scaffold { innerPadding ->
        if (!loaded) {
            return@Scaffold
        }
        if (dress == null) {
            Text(
                text = stringResource(R.string.dress_not_found),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(innerPadding).padding(16.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = dress.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = listOfNotNull(dress.className, dress.createdAt).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = if (dress.price != null) {
                            stringResource(R.string.dress_price_value, formatMoney(dress.price))
                        } else {
                            stringResource(R.string.dress_price_not_set)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(R.string.dress_photos), style = MaterialTheme.typography.titleMedium)
                    if (photos.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { photos.size })
                        HorizontalPager(
                            state = pagerState,
                            key = { photos[it].id },
                            modifier = Modifier.fillMaxWidth().height(260.dp).padding(top = 8.dp)
                        ) { page ->
                            val photo = photos[page]
                            Box(modifier = Modifier.fillMaxSize()) {
                                PhotoImage(
                                    path = photo.path,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { viewerIndex = page }
                                )
                                IconButton(
                                    onClick = { onDeletePhoto(photo) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = stringResource(R.string.dress_photo_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photos.size}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.dress_photos_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            TextButton(onClick = onPickPhoto) {
                                Text(stringResource(R.string.dress_photo_load))
                            }
                            TextButton(onClick = onTakePhoto, modifier = Modifier.padding(start = 8.dp)) {
                                Text(stringResource(R.string.dress_photo_camera))
                            }
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_edit_24),
                            contentDescription = stringResource(R.string.dress_edit_item)
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.dress_metrics),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showTakeMetric = true }) {
                            Text(stringResource(R.string.dress_metric_take))
                        }
                    }
                    if (metrics.isEmpty()) {
                        Text(
                            text = stringResource(R.string.taken_measurements_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        metrics.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editMetric = row }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = row.shortName, style = MaterialTheme.typography.titleMedium)
                                    Text(text = row.metricName, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(text = formatMeasure(row.value), style = MaterialTheme.typography.titleMedium)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.dress_comments),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showCommentDialog = true }) {
                            Text(stringResource(R.string.dress_comment_add))
                        }
                    }
                    if (comments.isEmpty()) {
                        Text(
                            text = stringResource(R.string.dress_comments_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        // Локальный порядок на время перетаскивания; в БД пишется по окончании жеста.
                        val orderedComments = remember(comments) {
                            mutableStateListOf<DressComment>().apply { addAll(comments) }
                        }
                        var draggingId by remember { mutableStateOf<Long?>(null) }
                        var dragOffsetY by remember { mutableFloatStateOf(0f) }
                        val itemBounds = remember { mutableStateMapOf<Long, Pair<Float, Float>>() }

                        orderedComments.forEach { comment ->
                            key(comment.id) {
                                val dragging = draggingId == comment.id
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(if (dragging) 1f else 0f)
                                        .graphicsLayer {
                                            translationY = if (dragging) dragOffsetY else 0f
                                            scaleX = if (dragging) 1.03f else 1f
                                            scaleY = if (dragging) 1.03f else 1f
                                            shadowElevation = if (dragging) 12f else 0f
                                        }
                                        .onGloballyPositioned { coords ->
                                            itemBounds[comment.id] =
                                                coords.positionInParent().y to coords.size.height.toFloat()
                                        }
                                        .pointerInput(comment.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingId = comment.id
                                                    dragOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y
                                                    val id = draggingId
                                                    val b = if (id != null) itemBounds[id] else null
                                                    if (id != null && b != null) {
                                                        val center = b.first + b.second / 2f + dragOffsetY
                                                        val currentIndex = orderedComments.indexOfFirst { it.id == id }
                                                        if (currentIndex >= 0) {
                                                            val pinnedCount = orderedComments.count { it.pinned }
                                                            val isPinned = orderedComments[currentIndex].pinned
                                                            var target = 0
                                                            for (other in orderedComments) {
                                                                if (other.id == id) continue
                                                                val ob = itemBounds[other.id] ?: continue
                                                                if (ob.first + ob.second / 2f < center) target++ else break
                                                            }
                                                            // Перетаскивание только внутри своей группы закрепления.
                                                            target = if (isPinned) {
                                                                target.coerceIn(0, pinnedCount - 1)
                                                            } else {
                                                                target.coerceIn(pinnedCount, orderedComments.size - 1)
                                                            }
                                                            if (target != currentIndex) {
                                                                val oldTop = b.first
                                                                val newTop = if (target > currentIndex) {
                                                                    oldTop + orderedComments
                                                                        .subList(currentIndex + 1, target + 1)
                                                                        .sumOf { (itemBounds[it.id]?.second ?: 0f).toDouble() }
                                                                        .toFloat()
                                                                } else {
                                                                    itemBounds[orderedComments[target].id]?.first ?: oldTop
                                                                }
                                                                if (target > currentIndex) {
                                                                    for (k in currentIndex + 1..target) {
                                                                        val oid = orderedComments[k].id
                                                                        itemBounds[oid]?.let {
                                                                            itemBounds[oid] = (it.first - b.second) to it.second
                                                                        }
                                                                    }
                                                                } else {
                                                                    for (k in target until currentIndex) {
                                                                        val oid = orderedComments[k].id
                                                                        itemBounds[oid]?.let {
                                                                            itemBounds[oid] = (it.first + b.second) to it.second
                                                                        }
                                                                    }
                                                                }
                                                                itemBounds[id] = newTop to b.second
                                                                val moved = orderedComments.removeAt(currentIndex)
                                                                orderedComments.add(target, moved)
                                                                dragOffsetY += oldTop - newTop
                                                            }
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    if (draggingId != null) {
                                                        onReorderComments(orderedComments.toList())
                                                    }
                                                    draggingId = null
                                                    dragOffsetY = 0f
                                                },
                                                onDragCancel = {
                                                    draggingId = null
                                                    dragOffsetY = 0f
                                                }
                                            )
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onToggleCommentPin(comment) }) {
                                            Icon(
                                                painter = painterResource(
                                                    if (comment.pinned) R.drawable.ic_push_pin
                                                    else R.drawable.ic_push_pin_outline
                                                ),
                                                contentDescription = stringResource(
                                                    if (comment.pinned) R.string.dress_comment_unpin
                                                    else R.string.dress_comment_pin
                                                ),
                                                tint = if (comment.pinned) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = comment.createdAt, style = MaterialTheme.typography.bodySmall)
                                        }
                                        TextButton(onClick = { editComment = comment }) {
                                            Text(stringResource(R.string.dress_comment_edit))
                                        }
                                        TextButton(onClick = { onDeleteComment(comment) }) {
                                            Text(
                                                text = stringResource(R.string.delete_btn),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCommentDialog) {
        CommentDialog(
            title = stringResource(R.string.dress_comment_add),
            onDismiss = { showCommentDialog = false },
            onConfirm = { text ->
                showCommentDialog = false
                onAddComment(text)
            }
        )
    }

    val commentToEdit = editComment
    if (commentToEdit != null) {
        CommentDialog(
            title = stringResource(R.string.dress_comment_edit_title),
            initialText = commentToEdit.text,
            onDismiss = { editComment = null },
            onConfirm = { text ->
                editComment = null
                onUpdateComment(commentToEdit, text)
            }
        )
    }

    val metricToEdit = editMetric
    if (metricToEdit != null) {
        EditMetricDialog(
            row = metricToEdit,
            onDismiss = { editMetric = null },
            onConfirm = { value ->
                editMetric = null
                onUpdateMetricValue(metricToEdit, value)
            }
        )
    }

    if (showTakeMetric && dress != null) {
        TakeMetricDialog(
            currentMetrics = metrics,
            loadClassMetrics = loadClassMetrics,
            loadAllMetrics = loadAllMetrics,
            loadNeighborMeasurements = loadNeighborMeasurements,
            classId = dress.classId,
            customerId = dress.customerId,
            onDismiss = { showTakeMetric = false },
            onConfirm = { metricId, value ->
                showTakeMetric = false
                onAddMetric(metricId, value)
            }
        )
    }

    val openedIndex = viewerIndex
    if (openedIndex != null && photos.isNotEmpty()) {
        PhotoViewerDialog(
            paths = photos.map { it.path },
            initialIndex = openedIndex,
            onDismiss = { viewerIndex = null }
        )
    }
}

@Composable
private fun EditMetricDialog(
    row: DressMetricRow,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var text by remember(row.metricId) { mutableStateOf(formatMeasure(row.value)) }
    val value = text.trim().replace(',', '.').toFloatOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${row.shortName} — ${row.metricName}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                label = { Text(stringResource(R.string.dress_metric_value_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { value?.let(onConfirm) },
                enabled = value != null
            ) { Text(stringResource(R.string.save_btn)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_btn)) }
        }
    )
}

@Composable
private fun CommentDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialText: String = ""
) {
    var text by remember(initialText) { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.dress_comment_hint)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) { Text(stringResource(R.string.save_btn)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_btn)) }
        }
    )
}

/**
 * «Снять мерку» с карточки изделия: сначала выбор мерки (сначала каркас класса, затем
 * остальной справочник), затем значение. Снятые значения других изделий этого клиента
 * показываются как min…max — по ним видно, куда попадёт новая мерка.
 * Нумерация «Мерка 1» — чтобы просто ткнуть и ввести, без выбора метрики.
 */
@Composable
private fun TakeMetricDialog(
    currentMetrics: List<DressMetricRow>,
    loadClassMetrics: suspend (Long) -> List<Metric>,
    loadAllMetrics: suspend () -> List<Metric>,
    loadNeighborMeasurements: suspend (Long) -> List<Triple<Long, String, String>>,
    classId: Long,
    customerId: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, Float) -> Unit
) {
    var classMetrics by remember { mutableStateOf<List<Metric>>(emptyList()) }
    var allMetrics by remember { mutableStateOf<List<Metric>>(emptyList()) }
    var neighbors by remember { mutableStateOf<Map<Long, Pair<String, String>>>(emptyMap()) }
    var selected by remember { mutableStateOf<Metric?>(null) }
    var valueText by remember { mutableStateOf("") }
    LaunchedEffect(classId, customerId) {
        classMetrics = runCatching { loadClassMetrics(classId) }.getOrDefault(emptyList())
        allMetrics = runCatching { loadAllMetrics() }.getOrDefault(emptyList())
        neighbors = runCatching { loadNeighborMeasurements(customerId) }
            .getOrDefault(emptyList())
            .associate { (metricId, min, max) -> metricId to (min to max) }
    }

    val takenIds = currentMetrics.map { it.metricId }.toSet()
    val palette = classMetrics + allMetrics.filter { m -> classMetrics.none { it.id == m.id } }
    val value = valueText.trim().replace(',', '.').toFloatOrNull()

    if (selected == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.dress_metric_take)) },
            text = {
                LazyColumn {
                    itemsIndexed(palette) { index, metric ->
                        val taken = metric.id in takenIds
                        val range = neighbors[metric.id]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = metric
                                    valueText = currentMetrics
                                        .firstOrNull { it.metricId == metric.id }
                                        ?.let { formatMeasure(it.value) }
                                        .orEmpty()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${metric.shortName} — ${metric.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val hint = buildList {
                                    if (taken) add(stringResource(R.string.dress_metric_already_taken))
                                    if (range != null) add(stringResource(R.string.dress_metric_hint_range, range.first, range.second))
                                }.joinToString(" · ")
                                if (hint.isNotEmpty()) {
                                    Text(text = hint, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_btn)) }
            }
        )
    } else {
        val metric = selected!!
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("${metric.shortName} — ${metric.name}") },
            text = {
                Column {
                    neighbors[metric.id]?.let { (min, max) ->
                        Text(
                            text = stringResource(R.string.dress_metric_hint_range, min, max),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                        label = { Text(stringResource(R.string.dress_metric_value_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { value?.let { onConfirm(metric.id, it) } },
                    enabled = value != null
                ) { Text(stringResource(R.string.save_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) {
                    Text(stringResource(R.string.dress_metric_choose_other))
                }
            }
        )
    }
}
