package com.example.seamstress.ui.screens.dress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.dresses.DressComment
import com.example.seamstress.domain.dresses.DressMetricRow
import com.example.seamstress.domain.dresses.DressPhoto
import com.example.seamstress.domain.dresses.DressRow
import com.example.seamstress.ui.components.PhotoImage
import com.example.seamstress.ui.components.formatMeasure
import com.example.seamstress.ui.components.formatMoney

/**
 * Карточка изделия: название · фото-слайдер (загрузить/снять камерой/удалить) ·
 * связанные мерки · стоимость · комментарии (добавить/удалить) · FAB «Редактировать».
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
    onAddComment: (String) -> Unit,
    onDeleteComment: (DressComment) -> Unit
) {
    var showCommentDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (dress != null) {
                ExtendedFloatingActionButton(onClick = onEdit) {
                    Text(stringResource(R.string.edit_measurement))
                }
            }
        }
    ) { innerPadding ->
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
                                    modifier = Modifier.fillMaxSize()
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
            }

            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.dress_metrics), style = MaterialTheme.typography.titleMedium)
                    if (metrics.isEmpty()) {
                        Text(
                            text = stringResource(R.string.taken_measurements_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        metrics.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        comments.forEach { comment ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.createdAt,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
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

    if (showCommentDialog) {
        AddCommentDialog(
            onDismiss = { showCommentDialog = false },
            onConfirm = { text ->
                showCommentDialog = false
                onAddComment(text)
            }
        )
    }
}

@Composable
private fun AddCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dress_comment_add)) },
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
