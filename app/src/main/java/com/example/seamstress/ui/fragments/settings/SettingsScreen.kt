package com.example.seamstress.ui.fragments.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R

@Composable
fun SettingsScreen(
    onMetricsClick: () -> Unit,
    onClassesClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SettingsItem(
            title = stringResource(R.string.metrics_reference),
            onClick = onMetricsClick
        )
        HorizontalDivider()
        SettingsItem(
            title = stringResource(R.string.classes_reference),
            onClick = onClassesClick
        )
        HorizontalDivider()
    }
}

@Composable
private fun SettingsItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}
