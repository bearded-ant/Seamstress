package com.example.seamstress.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.viewmodel.CustomerFormState

/**
 * Форма клиента для создания и редактирования: поля с ошибками, блокировка «Сохранить»
 * на время записи, у редактирования — ещё «Удалить» с подтверждением.
 */
@Composable
fun CustomerFormScreen(
    state: CustomerFormState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.ic_baseline_add_a_photo_24),
                    contentDescription = stringResource(R.string.customer_photo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(194.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.customer_name)) },
                        isError = state.nameError,
                        supportingText = if (state.nameError) {
                            { Text(stringResource(R.string.error_customer_name_empty)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = onPhoneChange,
                        label = { Text(stringResource(R.string.customer_phone_number)) },
                        isError = state.phoneError,
                        supportingText = if (state.phoneError) {
                            { Text(stringResource(R.string.error_customer_phone_invalid)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.balance,
                        onValueChange = onBalanceChange,
                        label = { Text(stringResource(R.string.customer_balance)) },
                        isError = state.balanceError,
                        supportingText = if (state.balanceError) {
                            { Text(stringResource(R.string.error_customer_balance_invalid)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Button(
                            onClick = onSave,
                            enabled = !state.saving,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(stringResource(R.string.save_btn))
                        }
                        TextButton(onClick = onCancel) {
                            Text(stringResource(R.string.cancel_btn))
                        }
                        if (onDelete != null) {
                            TextButton(onClick = { confirmDelete = true }) {
                                Text(
                                    text = stringResource(R.string.delete_btn),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_customer_title, state.name)) },
            text = { Text(stringResource(R.string.delete_customer_message, state.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        )
    }
}
