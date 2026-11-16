package com.example.seamstress.ui.screens.dress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.dress.DressClassItem
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.standards.StandardSetItem
import com.example.seamstress.ui.components.formatMeasure
import kotlinx.coroutines.launch

/**
 * Форма изделия для создания и правки: название, класс (каркас метрик, перевыбирается),
 * стоимость и значения мерок. Три источника значений: снять вручную, скопировать замер
 * клиента или именной набор общих мерок. Копия не привязана к источнику ссылкой.
 * Дата создания — служебное поле, в форме не редактируется.
 */
@Composable
fun DressFormScreen(
    classes: List<DressClassItem>,
    allMetrics: List<Metric>,
    classMetrics: List<Metric>,
    measurements: List<MeasurementRow>,
    standardSets: List<StandardSetItem>,
    saving: Boolean,
    onClassSelected: (Long) -> Unit,
    loadMeasurementValues: suspend (Long) -> List<Pair<Long, Float>>,
    loadSetValues: suspend (Long) -> List<Pair<Long, Float>>,
    onSave: (name: String, classId: Long, price: Long?, values: List<Pair<Long, Float>>) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
    initialName: String = "",
    initialClassId: Long? = null,
    initialPrice: String = "",
    initialValues: Map<Long, String> = emptyMap()
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedClassId by remember { mutableStateOf(initialClassId) }
    var priceText by remember { mutableStateOf(initialPrice) }
    val values = remember { mutableStateMapOf<Long, String>().apply { putAll(initialValues) } }
    val extraMetricIds = remember { mutableStateListOf<Long>() }

    var showClassDialog by remember { mutableStateOf(false) }
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var showSetDialog by remember { mutableStateOf(false) }
    var showExtraMetricDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var loadingSource by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectedClassName = classes.firstOrNull { it.id == selectedClassId }?.name.orEmpty()
    val extraMetrics = extraMetricIds.mapNotNull { id -> allMetrics.firstOrNull { it.id == id } }

    // Доп. метрики — те, что есть в значениях, но не входят в текущий каркас класса.
    LaunchedEffect(classMetrics) {
        val classIds = classMetrics.map { it.id }.toSet()
        extraMetricIds.removeAll { it in classIds }
        values.keys
            .filter { it !in classIds && it !in extraMetricIds }
            .forEach { extraMetricIds.add(it) }
    }

    val priceValue = priceText.trim().toLongOrNull()
    val priceValid = priceText.isBlank() || (priceValue != null && priceValue in 0..1_000_000_000)

    /** Источник подставляет значения целиком: каркас класса + лишние метрики источника. */
    fun applySource(pairs: List<Pair<Long, Float>>) {
        values.clear()
        extraMetricIds.clear()
        val classIds = classMetrics.map { it.id }.toSet()
        pairs.forEach { (metricId, value) ->
            values[metricId] = formatMeasure(value)
            if (metricId !in classIds) extraMetricIds.add(metricId)
        }
    }

    fun copyFrom(load: suspend () -> List<Pair<Long, Float>>) {
        scope.launch {
            loadingSource = true
            applySource(runCatching { load() }.getOrDefault(emptyList()))
            loadingSource = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dress_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedCard(
                    onClick = { showClassDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.measurement_class),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = selectedClassName.ifEmpty { stringResource(R.string.not_selected) },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit).take(10) },
                    label = { Text(stringResource(R.string.dress_price)) },
                    isError = !priceValid,
                    supportingText = if (!priceValid) {
                        { Text(stringResource(R.string.error_dress_price_invalid)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TextButton(
                onClick = { showMeasurementDialog = true },
                enabled = measurements.isNotEmpty() && !loadingSource,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.dress_from_measurement))
            }
            TextButton(
                onClick = { showSetDialog = true },
                enabled = standardSets.isNotEmpty() && !loadingSource,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.dress_from_set))
            }
        }

        if (classMetrics.isNotEmpty()) {
            Text(
                text = stringResource(R.string.class_metrics),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    classMetrics.forEach { metric ->
                        MetricValueField(
                            metric = metric,
                            value = values[metric.id].orEmpty(),
                            onValueChange = { values[metric.id] = it }
                        )
                    }
                }
            }
        }

        if (extraMetrics.isNotEmpty()) {
            Text(
                text = stringResource(R.string.extra_metrics),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    extraMetrics.forEach { metric ->
                        MetricValueField(
                            metric = metric,
                            value = values[metric.id].orEmpty(),
                            onValueChange = { values[metric.id] = it }
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = { showExtraMetricDialog = true },
            enabled = selectedClassId != null
        ) {
            Text(stringResource(R.string.add_extra_metric))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    val classId = selectedClassId ?: return@Button
                    val filled = (classMetrics.map { it.id } + extraMetricIds).mapNotNull { id ->
                        values[id].toMeasure()?.let { id to it }
                    }
                    onSave(
                        name.trim(),
                        classId,
                        if (priceText.isBlank()) null else priceValue,
                        filled
                    )
                },
                enabled = name.isNotBlank() && selectedClassId != null && priceValid && !saving && !loadingSource
            ) {
                Text(stringResource(R.string.save_btn))
            }
            TextButton(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.cancel_btn))
            }
            if (onDelete != null) {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.delete_btn),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showClassDialog) {
        AlertDialog(
            onDismissRequest = { showClassDialog = false },
            title = { Text(stringResource(R.string.measurement_class)) },
            text = {
                Column {
                    classes.forEach { dressClass ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedClassId = dressClass.id
                                    showClassDialog = false
                                    onClassSelected(dressClass.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedClassId == dressClass.id, onClick = null)
                            Text(dressClass.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClassDialog = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }

    if (showMeasurementDialog) {
        AlertDialog(
            onDismissRequest = { showMeasurementDialog = false },
            title = { Text(stringResource(R.string.dress_from_measurement)) },
            text = {
                Column {
                    measurements.forEach { measurement ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMeasurementDialog = false
                                    copyFrom { loadMeasurementValues(measurement.id) }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text("${measurement.name} · ${measurement.dateCreation}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMeasurementDialog = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }

    if (showSetDialog) {
        AlertDialog(
            onDismissRequest = { showSetDialog = false },
            title = { Text(stringResource(R.string.dress_from_set)) },
            text = {
                Column {
                    standardSets.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSetDialog = false
                                    copyFrom { loadSetValues(set.id) }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                stringResource(R.string.class_metrics_count, set.metricsCount)
                                    .let { count -> "${set.name} · $count" }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSetDialog = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }

    if (showExtraMetricDialog) {
        val available = allMetrics.filter {
            it.id !in classMetrics.map { metric -> metric.id } && it.id !in extraMetricIds
        }
        AlertDialog(
            onDismissRequest = { showExtraMetricDialog = false },
            title = { Text(stringResource(R.string.add_extra_metric)) },
            text = {
                Column {
                    available.forEach { metric ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    extraMetricIds.add(metric.id)
                                    showExtraMetricDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text("${metric.shortName} — ${metric.name}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExtraMetricDialog = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_dress_title, name)) },
            text = { Text(stringResource(R.string.delete_dress_message, name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) { Text(stringResource(R.string.dialog_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        )
    }
}

private fun String?.toMeasure(): Float? =
    this?.trim()?.replace(',', '.')?.toFloatOrNull()

@Composable
private fun MetricValueField(
    metric: Metric,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${metric.shortName} — ${metric.name}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }) },
            singleLine = true,
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(110.dp)
        )
    }
}
