package com.example.seamstress.ui.screens.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.domain.measured.taken.TakenMetricRow
import com.example.seamstress.ui.components.formatMeasure

@Composable
fun CardMeasurementScreen(
    measurement: MeasurementRow?,
    loaded: Boolean,
    taken: List<TakenMetricRow>,
    onEdit: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (measurement != null) {
                ExtendedFloatingActionButton(onClick = onEdit) {
                    Text(stringResource(R.string.edit_measurement))
                }
            }
        }
    ) { innerPadding ->
        if (!loaded) {
            return@Scaffold
        }
        if (measurement == null) {
            Text(
                text = stringResource(R.string.measurement_not_found),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(innerPadding).padding(16.dp)
            )
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = measurement.dateCreation,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = measurement.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = measurement.className,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (taken.isEmpty()) {
                Text(
                    text = stringResource(R.string.taken_measurements_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
                return@Column
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(taken, key = { it.metricId }) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = row.shortName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = row.metricName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = formatMeasure(row.value),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
