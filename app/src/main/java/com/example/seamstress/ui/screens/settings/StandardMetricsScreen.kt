package com.example.seamstress.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seamstress.R
import com.example.seamstress.domain.standards.StandardSetItem

/** Справочник именных наборов общих мерок («мужчина 54», «женщина 48»…). */
@Composable
fun StandardMetricsScreen(
    sets: List<StandardSetItem>,
    onAddSet: () -> Unit,
    onSetClick: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSet) {
                Text(text = "+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        if (sets.isEmpty()) {
            Text(
                text = stringResource(R.string.standard_sets_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(innerPadding).padding(16.dp)
            )
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            items(sets, key = { it.id }) { set ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetClick(set.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = set.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.class_metrics_count, set.metricsCount),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
