package com.example.seamstress.ui.fragments.measurement.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.ui.theme.SeamstressTheme

@Composable
fun AddMeasurementScreen(
    metrics: List<Metric>,
    onSave: (name: String, dressType: String, values: Map<Long, String>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dressType by remember { mutableStateOf("") }
    val values = remember { mutableStateMapOf<Long, String>() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.measurement_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dressType,
                    onValueChange = { dressType = it },
                    label = { Text(stringResource(R.string.measurement_type_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onSave(name.trim(), dressType.trim(), values.filterValues { it.isNotBlank() })
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(R.string.save_btn))
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        }

        if (metrics.isEmpty()) {
            Text(
                text = stringResource(R.string.metrics_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                items(metrics, key = { it.id }) { metric ->
                    OutlinedTextField(
                        value = values[metric.id].orEmpty(),
                        onValueChange = { values[metric.id] = it },
                        label = { Text(metric.shortName) },
                        supportingText = { Text(metric.name) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMeasurementScreenPreview() {
    SeamstressTheme {
        AddMeasurementScreen(
            metrics = listOf(
                Metric(1, 1, "Ог", "Обхват груди", null),
                Metric(2, 1, "От", "Обхват талии", null)
            ),
            onSave = { _, _, _ -> },
            onCancel = {}
        )
    }
}
