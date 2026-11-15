package com.example.seamstress.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric

@Composable
fun CreateClassScreen(
    metrics: List<Metric>,
    saving: Boolean,
    onSave: (name: String, metricIds: List<Long>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Long>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.class_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedIds.isNotEmpty()) {
            Text(
                text = stringResource(R.string.class_metrics),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            selectedIds.forEachIndexed { index, metricId ->
                val metric = metrics.firstOrNull { it.id == metricId } ?: return@forEachIndexed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}. ${metric.shortName} — ${metric.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            if (index > 0) {
                                val previous = selectedIds[index - 1]
                                selectedIds[index - 1] = metricId
                                selectedIds[index] = previous
                            }
                        },
                        enabled = index > 0
                    ) { Text("↑") }
                    TextButton(
                        onClick = {
                            if (index < selectedIds.lastIndex) {
                                val next = selectedIds[index + 1]
                                selectedIds[index + 1] = metricId
                                selectedIds[index] = next
                            }
                        },
                        enabled = index < selectedIds.lastIndex
                    ) { Text("↓") }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = stringResource(R.string.all_metrics),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        metrics.forEach { metric ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedIds.contains(metric.id),
                    onCheckedChange = { checked ->
                        if (checked) {
                            selectedIds.add(metric.id)
                        } else {
                            selectedIds.remove(metric.id)
                        }
                    }
                )
                Text(
                    text = "${metric.shortName} — ${metric.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onSave(name.trim(), selectedIds.toList()) },
                enabled = name.isNotBlank() && selectedIds.isNotEmpty() && !saving
            ) {
                Text(stringResource(R.string.save_btn))
            }
            TextButton(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.cancel_btn))
            }
        }
    }
}
