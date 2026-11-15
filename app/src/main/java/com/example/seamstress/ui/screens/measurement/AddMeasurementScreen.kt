package com.example.seamstress.ui.screens.measurement

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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.dress.DressClassItem
import com.example.seamstress.domain.measured.metric.Metric
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_PATTERN = "dd.MM.yyyy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementScreen(
    classes: List<DressClassItem>,
    allMetrics: List<Metric>,
    classMetrics: List<Metric>,
    saving: Boolean,
    onClassSelected: (Long) -> Unit,
    onSave: (date: String, name: String, classId: Long, values: List<Pair<Long, Float>>) -> Unit,
    onCancel: () -> Unit,
    initialDate: String? = null,
    initialName: String = "",
    initialClassId: Long? = null,
    initialValues: Map<Long, String> = emptyMap()
) {
    val formatter = remember { SimpleDateFormat(DATE_PATTERN, Locale.getDefault()) }
    var date by remember { mutableStateOf(initialDate ?: formatter.format(Date())) }
    var name by remember { mutableStateOf(initialName) }
    var selectedClassId by remember { mutableStateOf(initialClassId) }
    val selectedClassName = classes.firstOrNull { it.id == selectedClassId }?.name.orEmpty()

    val values = remember { mutableStateMapOf<Long, String>().apply { putAll(initialValues) } }
    val extraMetricIds = remember { mutableStateListOf<Long>() }

    // При правке доп. метрики — те, что сняты, но не входят в класс изделия.
    LaunchedEffect(classMetrics) {
        if (classMetrics.isNotEmpty()) {
            val classMetricIds = classMetrics.map { it.id }.toSet()
            initialValues.keys
                .filter { it !in classMetricIds && it !in extraMetricIds }
                .forEach { extraMetricIds.add(it) }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showClassDialog by remember { mutableStateOf(false) }
    var showExtraMetricDialog by remember { mutableStateOf(false) }

    val extraMetrics = extraMetricIds.mapNotNull { id -> allMetrics.firstOrNull { it.id == id } }

    val requiredIds = classMetrics.map { it.id } + extraMetricIds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.measurement_date),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(text = date, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.measurement_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                    onSave(
                        date,
                        name.trim(),
                        classId,
                        requiredIds.map { id -> id to (values[id].toMeasure() ?: 0f) }
                    )
                },
                enabled = name.isNotBlank() && selectedClassId != null && !saving
            ) {
                Text(stringResource(R.string.save_btn))
            }
            TextButton(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.cancel_btn))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            date = formatter.format(Date(it))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
                                    values.clear()
                                    extraMetricIds.clear()
                                    showClassDialog = false
                                    onClassSelected(dressClass.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedClassId == dressClass.id,
                                onClick = null
                            )
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
