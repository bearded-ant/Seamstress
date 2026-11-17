package com.example.seamstress.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.dresses.DressRow
import com.example.seamstress.ui.components.PhotoImage
import com.example.seamstress.ui.components.formatMoney
import com.example.seamstress.ui.components.formatPhone

@Composable
fun CustomerCardScreen(
    customer: Customers?,
    loaded: Boolean,
    dresses: List<DressRow>,
    onEdit: () -> Unit,
    onAddDress: () -> Unit,
    onDressClick: (Long) -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Scaffold { innerPadding ->
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
            CustomerHeader(
                customer = customer,
                onEdit = onEdit,
                onPickPhoto = onPickPhoto,
                onTakePhoto = onTakePhoto,
                onDeletePhoto = onDeletePhoto
            )

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
private fun CustomerHeader(
    customer: Customers,
    onEdit: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    var showPhotoMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp)
                    .clickable { showPhotoMenu = true }
            ) {
                PhotoImage(
                    path = customer.img,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Image(
                            painter = painterResource(R.drawable.ic_baseline_add_a_photo_24),
                            contentDescription = stringResource(R.string.customer_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_edit_24),
                        contentDescription = stringResource(R.string.edit_customer)
                    )
                }
            }
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
            }
        }
    }

    if (showPhotoMenu) {
        AlertDialog(
            onDismissRequest = { showPhotoMenu = false },
            title = { Text(stringResource(R.string.customer_photo_menu_title)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoMenu = false
                            onTakePhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.customer_photo_take)) }
                    TextButton(
                        onClick = {
                            showPhotoMenu = false
                            onPickPhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.customer_photo_pick)) }
                    if (customer.img != null) {
                        TextButton(
                            onClick = {
                                showPhotoMenu = false
                                onDeletePhoto()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.customer_photo_delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoMenu = false }) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }
}

@Composable
private fun DressTile(dress: DressRow, onClick: () -> Unit) {
    Card(modifier = Modifier.width(150.dp).height(180.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.fillMaxSize()) {
            PhotoImage(
                path = dress.photoPath,
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(
                    text = dress.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dress.className.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Text(
                    text = dress.createdAt,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
