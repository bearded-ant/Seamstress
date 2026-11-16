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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.dresses.DressRow
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.ui.components.PhotoImage
import com.example.seamstress.ui.components.formatMoney
import com.example.seamstress.ui.components.formatPhone

@Composable
fun CustomerCardScreen(
    customer: Customers?,
    loaded: Boolean,
    measurements: List<MeasurementRow>,
    dresses: List<DressRow>,
    onEdit: () -> Unit,
    onAddMeasurement: () -> Unit,
    onMeasurementClick: (Long) -> Unit,
    onAddDress: () -> Unit,
    onDressClick: (Long) -> Unit
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

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dress_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddDress) {
                    Text(stringResource(R.string.btn_add_clothes_text))
                }
            }

            if (dresses.isEmpty()) {
                Text(
                    text = stringResource(R.string.dress_list_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dresses, key = { it.id }) { dress ->
                        DressTile(
                            dress = dress,
                            onClick = { onDressClick(dress.id) }
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
            PhotoImage(
                path = customer.img,
                modifier = Modifier.fillMaxWidth().height(194.dp),
                placeholder = {
                    Image(
                        painter = painterResource(R.drawable.ic_baseline_add_a_photo_24),
                        contentDescription = stringResource(R.string.customer_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = formatPhone(customer.phone),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(R.string.customer_balance_value, formatMoney(customer.balance)),
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

@Composable
private fun DressTile(dress: DressRow, onClick: () -> Unit) {
    Card(modifier = Modifier.width(150.dp).height(150.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = dress.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = dress.className.orEmpty(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dress.createdAt,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
