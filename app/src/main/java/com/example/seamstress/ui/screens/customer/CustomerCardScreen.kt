package com.example.seamstress.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.measured.measurements.MeasurementRow

@Composable
fun CustomerCardScreen(
    customer: Customers?,
    loaded: Boolean,
    measurements: List<MeasurementRow>,
    onEdit: () -> Unit,
    onAddMeasurement: () -> Unit,
    onMeasurementClick: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (customer != null) {
                FloatingActionButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_edit_24),
                        contentDescription = stringResource(R.string.edit_customer)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (!loaded) {
            return@Scaffold
        }
        if (customer == null) {
            Text(
                text = stringResource(R.string.customer_not_found),
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
            CustomerHeader(customer = customer, onAddMeasurement = onAddMeasurement)

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            if (measurements.isEmpty()) {
                Text(
                    text = stringResource(R.string.taken_measurements_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(measurements, key = { it.id }) { measurement ->
                        MeasurementTile(
                            measurement = measurement,
                            onClick = { onMeasurementClick(measurement.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerHeader(customer: Customers, onAddMeasurement: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(R.drawable.ic_baseline_add_a_photo_24),
                contentDescription = stringResource(R.string.customer_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(194.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = customer.phone.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(R.string.customer_balance_value, customer.balance),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    TextButton(onClick = onAddMeasurement) {
                        Text(stringResource(R.string.take_measurements))
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementTile(measurement: MeasurementRow, onClick: () -> Unit) {
    Card(modifier = Modifier.width(150.dp).height(150.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = measurement.dateCreation,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = measurement.className,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = measurement.name,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
