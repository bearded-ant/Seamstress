package com.example.seamstress.ui.screens.settings

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
import androidx.compose.runtime.getValue
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

/**
 * Форма именного набора общих мерок: имя + значения метрик.
 * Каркас — класс изделия или весь справочник. Создание и правка одним экраном;
 * у правки есть «Удалить» с подтверждением.
 */
@Composable
fun StandardSetFormScreen(
    classes: List<DressClassItem>,
    allMetrics: List<Metric>,
    classMetrics: List<Metric>,
    saving: Boolean,
    onClassSelected: (Long) -> Unit,
    onSave: (name: String, values: List<Pair<Long, Float>>) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
    initialName: String = "",
    initialClassId: Long? = null,
    initialValues: Map<Long, String> = emptyMap()
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedClassId by remember { mutableStateOf(initialClassId) }
    val values = remember { mutableStateMapOf<Long, String>().apply { putAll(initialValues) } }

    var showClassDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // Каркас: выбранный класс или весь справочник.
    val skeletonMetrics = if (selectedClassId == null) allMetrics else classMetrics
    val selectedClassName = classes.firstOrNull { it.id == selectedClassId }?.name

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
                    label = { Text(stringResource(R.string.standard_set_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedCard(
                    onClick = { showClassDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.standard_set_skeleton),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = selectedClassName
                                ?: stringResource(R.string.standard_set_all_metrics),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (skeletonMetrics.isNotEmpty()) {
            Text(
                text = stringResource(R.string.standard_set_values),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    skeletonMetrics.forEach { metric ->
                        MetricValueField(
                            metric = metric,
                            value = values[metric.id].orEmpty(),
                            onValueChange = { values[metric.id] = it }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    val filled = skeletonMetrics.mapNotNull { metric ->
                        values[metric.id].toMeasure()?.let { metric.id to it }
                    }
                    onSave(name.trim(), filled)
                },
                enabled = name.isNotBlank() && !saving
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
            title = { Text(stringResource(R.string.standard_set_skeleton)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedClassId = null
                                showClassDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedClassId == null, onClick = null)
                        Text(stringResource(R.string.standard_set_all_metrics))
                    }
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

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_standard_set_title, name)) },
            text = { Text(stringResource(R.string.delete_standard_set_message, name)) },
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
